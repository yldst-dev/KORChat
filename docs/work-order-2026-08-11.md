# 작업 지시서 2026-08-11

docs/opus5-meta-prompt.md의 지시서를 먼저 읽고 그 제약을 전제로 아래 세 작업을 순서대로 수행한다. 작업마다 커밋을 분리한다.

## 작업 1. 의존성 최신 패치 버전 반영

목적: Minecraft 26.2 호환 범위 안에서 의존성을 최신 패치 버전으로 올린다.

- gradle.properties의 다음 값에 대해 최신 버전을 실제로 조회해 확인한다. 기억으로 단정하지 않는다.
  - minecraft_version (26.2.x 패치 릴리스 존재 여부 확인, 있으면 minecraft_version_range도 함께 검토)
  - fabric_version, fabric_loader_version
  - neoforge_version, neo_form_version
  - yacl_version, cloth_config_version, modmenu_version
- 조회처: Modrinth API, Fabric meta(meta.fabricmc.net), NeoForge maven(maven.neoforged.net), 각 모드의 Modrinth 페이지.
- 원칙: 26.2 호환 범위를 벗어나는 메이저/마이너 상향은 하지 않는다. 26.3 대응은 이번 작업 범위가 아니다.
- 버전을 올린 뒤 `./gradlew build`로 fabric과 neoforge 양쪽 컴파일을 확인한다.
- 올리지 않은 항목이 있으면 이유(이미 최신, 호환 범위 밖 등)를 보고에 적는다.

## 작업 2. 한/영 전환 키를 게임 단축키 설정에서 변경 가능하게

목적: 사용자가 Minecraft 설정 > 조작(단축키) 화면에서 한/영 전환 키와 IME 토글 키를 자유롭게 바꿀 수 있고, 바꾼 키가 실제로 동작해야 한다.

현재 상태 (참고):

- common/src/main/java/com/hyfata/najoan/koreanpatch/client/KeyBinds.java가 KeyMapping 두 개(toggle_langtype, toggle_ime)를 자체 카테고리로 등록한다. 기본값은 Windows=Right Alt, macOS=Caps Lock, 기타=Left Control.
- common/src/main/java/com/hyfata/najoan/koreanpatch/mixin/KeyboardHandlerMixin.java의 keyPress 인젝션이 `KeyBinds.getLangBinding().matches(keyEvent)`로 키를 판정한다.

수행할 일:

1. fabric과 neoforge 모듈에서 이 KeyMapping들이 실제로 로더에 등록되어 단축키 설정 화면에 나타나는지 확인한다. 누락되어 있으면 로더별 등록 코드를 추가한다 (Fabric: KeyBindingHelper, NeoForge: RegisterKeyMappingsEvent).
2. 단축키 설정에서 키를 바꿨을 때 KeyboardHandlerMixin 경로가 바뀐 키로 동작하는지 확인한다. matches()가 커버하지 못하는 경우가 있으면 수정한다.
3. IME 토글 판정의 `modifiers == 2` 하드코딩(Ctrl 강제)을 점검한다. 사용자가 modifier 없는 키로 재바인딩하면 동작하지 않는 구조라면, 바인딩된 키 자체로 판정하도록 고친다.
4. en_us.json과 ko_kr.json에 키바인딩 이름과 카테고리 번역이 있는지 확인하고 없으면 추가한다.
5. 검증: 빌드 후 클라이언트를 실행해 단축키 화면에서 키를 변경하고, 채팅창에서 변경된 키로 한/영 전환이 되는지 확인한다. 실행 확인을 못 했으면 그 사실을 보고에 적는다.

## 작업 3. macOS에서 Karabiner로 재매핑된 키(F18 등) 지원

목적: macOS 사용자가 Karabiner-Elements로 한/영 키를 F18 등 임의 키로 매핑해 쓰는 경우, 그 키를 단축키 설정에 바인딩하면 정상 동작해야 한다.

현재 상태 (참고, KeyboardHandlerMixin 기준):

- lang 키 판정에 `(!Platform.isMac() || modifiers != 1 && modifiers != 2)` 조건이 있어 macOS에서 Shift/Ctrl modifier가 붙은 이벤트를 걸러낸다. Caps Lock 전제의 방어 로직이다.
- `Platform.isMac() && action == 0 && keyCode == -1 && scanCode == 255` 분기가 Caps Lock 해제를 감지해 DarwinController.isCapsLockOn()이 참이면 강제로 영문 상태로 되돌린다.
- DarwinController가 시스템 Caps Lock 상태와 연동된다.

수행할 일:

1. macOS 전용 분기들을 "바인딩이 Caps Lock일 때"로 한정한다. 바인딩된 키가 Caps Lock이 아니면 Caps Lock 보정과 modifier 필터가 개입하지 않아야 한다. 현재 바인딩 키는 KeyMapping에서 조회할 수 있다.
2. F18(GLFW_KEY_F18) 같은 키가 GLFW keyCode로 정상 전달되는 경우와, keyCode가 -1로 오고 scanCode만 있는 경우를 모두 처리한다. 후자는 InputConstants.Type.SCANCODE 바인딩과 matches() 판정이 커버하는지 확인하고, 안 되면 scanCode 기반 판정을 보강한다.
3. Karabiner가 키를 가로채 OS 레벨에서 변환하므로 게임에는 변환된 최종 키만 도착한다는 전제로 구현한다. Karabiner 자체를 감지하거나 연동하려 하지 않는다.
4. 기존 Caps Lock 기본 동작은 그대로 유지되어야 한다. Caps Lock으로 쓰는 기존 사용자에게 회귀가 없어야 한다.
5. 검증: 빌드 확인은 필수. macOS 실기 테스트가 가능하면 Caps Lock 바인딩과 F18 바인딩 두 경우를 모두 확인하고, 불가능하면 어떤 경우를 확인하지 못했는지 보고에 적는다.

## 공통 완료 조건

- 세 작업 모두 `./gradlew build` 통과.
- 작업별 커밋 분리, 커밋 메시지는 영어 한 줄 명령형.
- 최종 보고에 작업별로 변경 내용, 검증 방법, 확인하지 못한 항목을 구분해 적는다.
