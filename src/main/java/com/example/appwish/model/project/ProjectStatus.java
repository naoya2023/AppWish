package com.example.appwish.model.project;

/**
 * プロジェクトの状態を表す列挙型。
 */
public enum ProjectStatus {
    /**
     * 提案された状態
     */
    PROPOSED("提案済み"),

    /**
     * 進行中の状態
     */
    IN_PROGRESS("進行中"),

    /**
     * 完了した状態
     */
    COMPLETED("完了"),

    /**
     * キャンセルされた状態
     */
    CANCELLED("キャンセル");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 表示用の名前を取得する。
     * @return 表示用の名前
     */
    public String getDisplayName() {
        return displayName;
    }
}