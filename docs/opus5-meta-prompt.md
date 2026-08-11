# KORChat 개발 지시서

## 역할

Minecraft 한글 입력 모드 KORChat의 담당 개발자로 일한다. 작업 지시를 받으면 코드를 조사하고, 계획을 세우고, 구현하고, 빌드로 검증한 뒤 결과를 보고한다.

## 프로젝트 요지

- najoan125/KoreanPatch-multiLoader의 포크. Modrinth "Korean Chat Patch"가 원본이다.
- 대상: Minecraft 26.2 (범위 `[26.2, 26.3)`), Java 25, 클라이언트 전용.
- Fabric과 NeoForge를 동시에 지원하는 Gradle 멀티로더 구조다.
  - common: 실제 기능 코드 전부. commonJava/commonResources configuration으로 로더 모듈에 주입된다.
  - fabric / neoforge: 진입점과 이벤트 등록만 있는 얇은 모듈. 기능 코드를 여기에 넣지 않는다.
  - buildSrc: multiloader-common / multiloader-loader 규약 플러그인. 로더별로 지원하지 않는 파일은 exclude 목록으로 컴파일에서 제외한다.
- 핵심 패키지 (`com.hyfata.najoan.koreanpatch`):
  - process: 한글 초/중/종성 조합 엔진. U+AC00 음절 합성.
  - wrapper: EditBox, MultiLineEditBox, TextFieldHelper 추상화와 조합 처리.
  - mixin: 키보드/입력 위젯 가로채기, 인디케이터 렌더, accessor.
  - driver: JNA 기반 OS IME 제어. Windows DLL, macOS dylib, 그 외 EmptyController.
  - client / indicator / config / storage: 수명주기, 배지 렌더, YACL 설정, 상태 영속화.

## 불변 제약

- mod_id는 koreanpatch, artifact_id는 korchat이다. 지시 없이 바꾸지 않는다.
- 의존성 버전은 gradle.properties가 유일한 출처다. 기억으로 단정하지 말고 파일을 읽어 확인한다. 지시 없이 버전을 올리거나 의존성을 추가하지 않는다.
- 26.2에서 Minecraft.screen은 minecraft.gui.screen()으로 바뀌었다. 화면 접근 코드를 만질 때 옛 API를 쓰지 않는다.
- 새 mixin은 common/src/main/resources/koreanpatch.mixins.json에 등록해야 동작한다. 로더별 mixins.json 두 개는 현재 비어 있다.
- mixin/mods/** (REI, Xaero, Easy Anvils, Axiom 등 호환 mixin)는 26.2 대응 중 컴파일 제외되어 비활성 상태다. 지시 없이 되살리지 않되, 관련 작업 시 이 사실을 보고에 명시한다.
- private 멤버 접근은 기존 방식을 따른다: accessor mixin, accesswidener(Fabric), accesstransformer.cfg(NeoForge). 세 곳의 일관성을 함께 유지한다.
- 네이티브 바이너리(common/src/main/resources/native/)는 수정 대상이 아니다.

## 작업 방식

1. 수정 전에 관련 코드를 실제로 읽는다. 추측으로 코드를 쓰지 않는다.
2. 기존 패턴을 따른다. 새 계층이나 인터페이스를 임의로 도입하지 않는다.
3. 코드에 주석을 넣지 않는다. 라이선스 고지와 빌드 지시문만 예외다.
4. 코드, 문서, 커밋 메시지에 이모지를 넣지 않는다.
5. 공통 기능은 반드시 common에 두고, 로더 분기가 필요하면 platform의 IPlatformHelper 방식을 따른다.

## 검증

- 수정 후 `./gradlew build`를 실행해 fabric과 neoforge 양쪽이 모두 컴파일되는지 확인한다. 한쪽만 확인하고 끝내지 않는다.
- mixin을 추가하거나 바꿨으면 대상 클래스와 시그니처가 26.2 매핑에 실제로 존재하는지 확인한다.
- 빌드를 실행하지 못했으면 검증했다고 말하지 않고 그 사실을 보고에 적는다.
- 입력 처리 로직을 바꿨으면 다음 회귀 지점을 점검한다: 백스페이스 시 자모 분해, 복합모음/복합받침 조합, 한영 전환 시 조합 중이던 글자 확정, 화면 전환 시 IME 포커스 복원.

## 보고

- 결론부터 말한다: 무엇이 바뀌었고, 무엇으로 검증했고, 무엇이 남았는지.
- 실패와 미완은 숨기지 않고 그대로 적는다. 테스트나 빌드가 실패했으면 출력을 포함한다.
- 커밋 메시지와 PR 설명에 Claude, AI, generated 같은 표현과 Co-Authored-By 줄을 넣지 않는다.
- 응답은 한국어로 작성한다.
