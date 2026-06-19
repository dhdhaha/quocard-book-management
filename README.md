# 書籍管理システム API（クオカード コーディングテスト）

書籍と著者を管理するバックエンド REST API です。  
技術スタック: **Kotlin / Spring Boot / jOOQ / Flyway / PostgreSQL**

詳細な設計書は [`設計/設計書.md`](設計/設計書.md) を参照してください。

---

## 必要な環境

| ツール | バージョン | 用途 | 状態 |
|--------|-----------|------|------|
| **JDK** | 21（17 も可） | アプリケーション実行・ビルド | ✅ インストール済み |
| **Docker Desktop** | 最新版 | PostgreSQL コンテナ起動 | ✅ インストール済み |
| **Gradle** | ラッパー同梱 | ビルド | ✅ `gradlew` 使用（別途インストール不要） |
| **IntelliJ IDEA** | Community 版以上 | 開発 IDE | 任意（推奨） |
| **Git** | 最新版 | バージョン管理・GitHub 提出 | 要確認 |

### インストール済みソフトウェアのパス

```
JDK 21 : C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot
Docker : C:\Program Files\Docker\Docker\resources\bin
```

### 環境変数（推奨設定）

PowerShell で以下を実行するか、システム環境変数に登録してください。

```powershell
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot", "User")
```

設定後、ターミナルを再起動して確認します。

```powershell
java -version
# openjdk version "21.0.11" が表示されれば OK

docker --version
# Docker version 29.x が表示されれば OK
```

---

## 初回セットアップ

### 1. Docker Desktop の起動

1. スタートメニューから **Docker Desktop** を起動する
2. 初回起動時は WSL2 のセットアップや再起動を求められる場合がある（画面の指示に従う）
3. タスクバーのクジラアイコンが「Running」になるまで待つ

```powershell
docker info
# Server セクションが表示されれば起動完了
```

### 2. プロジェクトの取得・移動

```powershell
cd C:\Users\nemo9\quocard-coding-test\quocard-book-management
```

### 3. 初回ビルド手順（重要）

Flyway は **Spring Boot 起動時**にのみマイグレーションを実行します（クオカード例に準拠）。  
jOOQ コード生成には **マイグレーション済みの DB** が必要なため、初回は次の順序で実行してください。

```powershell
# 1. PostgreSQL 起動
docker compose up -d

# 2. Spring Boot を一度起動して Flyway マイグレーションを適用（起動ログ確認後 Ctrl+C で停止）
.\gradlew.bat bootRun

# 3. jOOQ コード生成 + ビルド
.\gradlew.bat jooqCodegen build
```

2回目以降（DB スキーマが既にある場合）は `.\gradlew.bat build` のみで構いません。

### 4. ビルド・テスト確認

```powershell
.\gradlew.bat test
```

> PostgreSQL コンテナが起動している状態で実行してください。

---

## アプリケーションの起動

Spring Boot の Docker Compose サポートにより、`bootRun` 実行時に PostgreSQL コンテナが自動起動します。

```powershell
.\gradlew.bat bootRun
```

起動後、以下で動作確認できます。

```
http://localhost:8080/api/authors   （POST で著者登録など）
```

> **`http://localhost:8080/` だけを開くと 404 が表示されます。正常です。**  
> フロントエンドはなく、ルート URL にはページを定義していません。

### データベース接続情報（Docker Compose）

| 項目 | 値 |
|------|-----|
| ホスト | `localhost` |
| ポート | `5432` |
| データベース名 | `mydatabase` |
| ユーザー | `myuser` |
| パスワード | `secret` |

---

## テストの実行

```powershell
.\gradlew.bat test
```

---

## プロジェクト構成

```
quocard-book-management/
├── 設計/
│   ├── 設計書.md          # 日本語 設計書
│   └── 설계서.md          # 韓国語 設計書
├── src/
│   ├── main/
│   │   ├── kotlin/        # アプリケーションコード
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/   # Flyway マイグレーション（実装時に追加）
│   └── test/
│       └── kotlin/        # 単体テスト
├── compose.yaml           # PostgreSQL Docker Compose 定義
├── build.gradle           # Gradle ビルド設定
├── gradle.properties      # JDK 21 パス設定
└── README.md              # 本ファイル
```

---

## API エンドポイント

| Method | Path | 説明 | 状態 |
|--------|------|------|------|
| POST | `/api/authors` | 著者登録 | ✅ 実装済み |
| PUT | `/api/authors/{id}` | 著者更新 | ✅ 実装済み |
| GET | `/api/authors/{id}/books` | 著者の書籍一覧 | ✅ 実装済み |
| POST | `/api/books` | 書籍登録 | ✅ 実装済み |
| PUT | `/api/books/{id}` | 書籍更新 | ✅ 実装済み |
| GET | `/api/books/{id}` | 書籍詳細 | ✅ 実装済み |

### リクエスト例

**著者登録**
```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name":"山田太郎","birthDate":"1980-01-15"}'
```

**書籍登録**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Kotlin入門","price":2800,"publicationStatus":"UNPUBLISHED","authorIds":[1]}'
```

---

## jOOQ コード生成について

[jOOQ 公式 Gradle プラグイン](https://www.jooq.org/doc/3.19/manual/code-generation/codegen-gradle/)（`org.jooq.jooq-codegen-gradle`）を使用しています。

```powershell
docker compose up -d
.\gradlew.bat bootRun          # 初回のみ: Flyway マイグレーション適用
.\gradlew.bat jooqCodegen      # DB スキーマからコード生成
.\gradlew.bat build
```

生成先: `build/generated-src/jooq/main`  
`flyway_schema_history` テーブルは生成対象から除外しています。

---

## IntelliJ IDEA での開発

1. **File → Open** で `quocard-book-management` フォルダを開く
2. **File → Project Structure → Project SDK** で JDK 21 を選択
   - パス: `C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot`
3. Gradle の自動インポートを待つ
4. `BookManagementApplication.kt` を右クリック → **Run**

---

## トラブルシューティング

### `docker: command not found`

- Docker Desktop が起動していない、またはターミナルを再起動していない
- ターミナルを閉じて開き直す

### `java` のバージョンが 8 や 17 のまま

```powershell
# 現在の java パスを確認
where.exe java

# JAVA_HOME を JDK 21 に設定（上記「環境変数」参照）
# ターミナルを再起動
```

### `bootRun` 時に PostgreSQL 接続エラー

1. Docker Desktop が起動しているか確認
2. ポート 5432 が他のアプリに使われていないか確認

```powershell
docker compose -f compose.yaml up -d
docker compose -f compose.yaml ps
```

### Gradle ビルドが遅い

初回のみ依存ダウンロードに時間がかかります。2 回目以降はキャッシュが効きます。

---

## GitHub 提出について

- リポジトリは **public** で作成すること
- README に実行方法を記載すること（本ファイルをそのまま利用可）
- 提出 URL をクオカードに送付すること

---

## 参考リンク

- [Spring Initializr](https://start.spring.io/)
- [jOOQ コード生成（Gradle）](https://www.jooq.org/doc/3.19/manual/code-generation/codegen-gradle/)
- [Flyway ドキュメント](https://flywaydb.org/documentation/)
- [クオカード コーディングテスト説明](設計/設計書.md)
