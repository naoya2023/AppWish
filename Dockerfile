# ベースイメージとしてOpenJDK 17を使用
FROM openjdk:17-jdk-slim

# 作業ディレクトリを設定
WORKDIR /app

# Mavenをインストール
RUN apt-get update && \
    apt-get install -y maven

# pom.xmlとソースコードをコピー
COPY pom.xml .
COPY src ./src

# アプリケーションをビルド
RUN mvn clean package -DskipTests

# JARファイルを実行
CMD ["java", "-jar", "target/AppWish-0.0.1-SNAPSHOT.jar"]
