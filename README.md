# 書籍管理システム API（クオカード コーディングテスト）

**リポジトリ:** https://github.com/dhdhaha/quocard-book-management

書籍と著者を管理するバックエンド REST API です。  
技術スタック: **Kotlin / Spring Boot / jOOQ / Flyway / PostgreSQL**

詳細な技術スタックやAPIの仕様については、ソースコードおよびテストコードをご参照ください。

---

## 必要な環境

| ツール | バージョン | 用途 |
|--------|-----------|------|
| **JDK** | 21（17 も可） | アプリケーション実行・ビルド |
| **Docker Desktop** | 最新版 | PostgreSQL コンテナ起動 |
| **Gradle** | ラッパー同梱 | ビルド（`gradlew` 使用） |

```powershell
java -version   # JDK 21 推奨
docker --version
```

---

## 初回セットアップ

### 1. リポジトリの clone

```powershell
git clone https://github.com/dhdhaha/quocard-book-management.git
cd quocard-book-management
```

### 2. PostgreSQL 起動

Docker Desktop を起動したうえで:

```powershell
docker compose up -d
```

### 3. テストと起動

生成済みの jOOQ クラスをコミットしているため、クローン直後でも追加作業なしでビルド・テストが可能です。Flyway マイグレーションは Spring Boot 起動時（またはテスト実行時）に自動適用されます。

```powershell
# テストの実行
.\gradlew.bat test

# アプリケーションの起動
.\gradlew.bat bootRun
```

> **注意:** 実行には PostgreSQL コンテナが起動している必要があります。

---

## アプリケーションの起動

```powershell
.\gradlew.bat bootRun
```

起動後の API 例: `http://localhost:8080/api/authors`

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

## API エンドポイント

| Method | Path | 説明 |
|--------|------|------|
| POST | `/api/authors` | 著者登録 |
| PUT | `/api/authors/{id}` | 著者更新 |
| GET | `/api/authors/{id}/books` | 著者の書籍一覧 |
| POST | `/api/books` | 書籍登録 |
| PUT | `/api/books/{id}` | 書籍更新 |
| GET | `/api/books/{id}` | 書籍詳細（仕様外・利便のため実装） |

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

## プロジェクト構成

```
quocard-book-management/
├── src/main/java/         # jOOQ 生成済みクラス
├── src/main/kotlin/       # アプリケーションコード
├── src/main/resources/db/migration/   # Flyway
├── src/test/kotlin/       # 統合テスト・WebMvcTest
├── compose.yaml
└── README.md
```

---

## jOOQ コード生成

[jOOQ 公式 Gradle プラグイン](https://www.jooq.org/doc/3.19/manual/code-generation/codegen-gradle/)を使用しています。  
DBスキーマを変更した場合は、以下の手順で jOOQ クラスを再生成してください。

```powershell
docker compose up -d
.\gradlew.bat bootRun          # Flyway マイグレーションを適用（起動確認後停止）
.\gradlew.bat jooqCodegen      # コードを再生成
```

生成先: `src/main/java/com/quocard/bookmanagement/jooq`

---

## トラブルシューティング

**`docker: command not found`** — Docker Desktop が起動しているか確認し、ターミナルを再起動してください。

**`bootRun` 時に PostgreSQL 接続エラー** — `docker compose up -d` でコンテナを起動してください。

**Gradle ビルドが遅い** — 初回のみ依存ダウンロードに時間がかかります。

---

## 参考リンク

- [jOOQ コード生成（Gradle）](https://www.jooq.org/doc/3.19/manual/code-generation/codegen-gradle/)
- [Flyway ドキュメント](https://flywaydb.org/documentation/)
