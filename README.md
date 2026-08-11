# KORChat

KORChat은 Minecraft `26.2`용 한글 채팅 및 입력 모드입니다. 이 저장소는 `Korean Patch`를 기반으로 유지보수와 26.2 대응을 진행한 포크입니다.

## 지원 환경

- Minecraft `26.2`
- Java `25`
- Fabric
- NeoForge

## 주요 기능

- 채팅창, 아이템 검색창, 표지판, 책 입력창 등 주요 입력 화면에서 한글 입력 지원
- 한/영 상태 표시기 제공
- 일반 입력과 IME 입력을 함께 다룰 수 있는 입력 보정 기능

## 설치

1. 사용 중인 로더에 맞는 JAR 하나만 선택합니다.
2. `mods` 폴더에 넣습니다.
3. 게임을 완전히 다시 실행합니다.

## 조작

| 플랫폼 | 한/영 전환 | IME 전환 |
| --- | --- | --- |
| Windows | `한/영 (Right Alt)` | `Left Control + I` |
| macOS | `Caps Lock` | 지원하지 않음 |
| Linux | `Left Control` | 지원하지 않음 |

위 값은 기본값이며, `설정 > 조작 > 키 설정`의 `KORChat` 항목에서 원하는 키로 바꿀 수 있습니다.

IME 전환 키를 기본값에서 다른 키로 바꾸면 `Left Control` 조합 없이 그 키만 눌러도 전환됩니다. 기본값인 `I`를 그대로 쓰는 동안에는 채팅 입력과 겹치지 않도록 `Left Control` 조합을 계속 요구합니다.

macOS에서 Karabiner-Elements 등으로 한/영 키를 `F18` 같은 다른 키로 재매핑해 쓰는 경우, 그 키를 한/영 전환에 바인딩하면 그대로 동작합니다. Caps Lock 전용 보정 동작은 한/영 전환이 실제로 Caps Lock에 바인딩되어 있을 때만 적용됩니다.

## 변경 사항

### 26.2.1

- 한/영 전환 키와 IME 전환 키를 `설정 > 조작 > 키 설정`에서 바꾼 값이 실제 입력 판정에 반영되도록 수정했습니다.
- IME 전환 키를 기본값이 아닌 키로 바꾼 경우 `Left Control` 조합을 강제하지 않도록 했습니다.
- macOS의 Caps Lock 전용 보정 동작을 한/영 전환이 Caps Lock에 바인딩된 경우로 한정했습니다. Karabiner-Elements 등으로 `F18` 같은 키에 재매핑해 쓰는 환경을 지원합니다.
- 키 설정 화면의 `KORChat` 항목이 올바른 위치에 표시되도록 키바인딩 카테고리 등록 방식을 26.2 API에 맞췄습니다.
- 의존성을 26.2 호환 범위 안에서 최신 패치 버전으로 올렸습니다. Fabric API `0.157.0`, NeoForge `26.2.0.59`, NeoForm `26.2-2`, YACL `3.9.6`, Mod Menu `20.0.1`.

### 26.2.0

- Minecraft `26.2` 대응.

## 원본 프로젝트

- 원본 제작자: [Najoan](https://github.com/najoan125)
- 원본 저장소: [najoan125/KoreanPatch-multiLoader](https://github.com/najoan125/KoreanPatch-multiLoader)
- 원본 배포 페이지: [Modrinth - Korean Chat Patch](https://modrinth.com/mod/korean-chat-patch)

## 라이선스

이 프로젝트는 원본과 동일하게 `LGPL-3.0`을 유지합니다. 세부 내용은 [LICENSE](./LICENSE)를 확인하십시오.

## 기여 및 이슈

이 포크 저장소 관련 기여와 이슈는 아래 링크를 사용하십시오.

- Issues: [GitHub Issues](https://github.com/yldst-dev/KORChat/issues)
- Source: [GitHub Repository](https://github.com/yldst-dev/KORChat)

## 크레딧

- Original author: [Najoan](https://github.com/najoan125)
- Contributor: [Shihyeon](https://github.com/Shihyeon)
