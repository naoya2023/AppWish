package com.example.appwish.model.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * プロジェクトのカテゴリを表す列挙型。
 */
public enum ProjectCategory {
    /**
     * Webアプリケーション
     */
    WEB_APPLICATION("エンタメ"),

    /**
     * モバイルアプリ
     */
    MOBILE_APP("ライフスタイル"),

    /**
     * デスクトップアプリケーション
     */
    DESKTOP_APPLICATION("ビジネス"),

    /**
     * ゲーム
     */
    GAME("金融"),

    /**
     * AI/機械学習
     */
    AI_ML("ファッション"),

    /**
     * IoT (Internet of Things)
     */
    IOT("教育"),

    /**
     * その他
     */
    OTHER("その他");

    private final String displayName;

    ProjectCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * カテゴリの表示名を取得する。
     * @return カテゴリの表示名
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 表示名からProjectCategoryを取得する。
     * @param displayName 表示名
     * @return 対応するProjectCategory、見つからない場合はnull
     */
    @JsonCreator
    public static ProjectCategory fromDisplayName(String displayName) {
        for (ProjectCategory category : ProjectCategory.values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 名前（列挙型の定数名）からProjectCategoryを取得する。
     * @param name 名前
     * @return 対応するProjectCategory
     * @throws IllegalArgumentException 対応するProjectCategoryが見つからない場合
     */
    public static ProjectCategory fromString(String name) {
        try {
            return ProjectCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fromDisplayName(name);
        }
    }
}