package ru.mindlex.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Персональные настройки пользователя и агрегаты геймификации (стрик, рекорды).
 * Первичный ключ совпадает с идентификатором пользователя.
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "daily_goal", nullable = false)
    private int dailyGoal = 15;

    @Column(name = "language", nullable = false, length = 8)
    private String language = "en";

    @Column(name = "selected_deck_id")
    private UUID selectedDeckId;

    @Column(name = "preferred_time", nullable = false)
    private LocalTime preferredTime = LocalTime.of(19, 0);

    @Column(name = "rush_seconds", nullable = false)
    private int rushSeconds = 90;

    @Column(name = "cloze_seconds", nullable = false)
    private int clozeSeconds = 35;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "last_study_date")
    private LocalDate lastStudyDate;

    @Column(name = "best_rush_score", nullable = false)
    private int bestRushScore;

    @Column(name = "max_combo_record", nullable = false)
    private int maxComboRecord;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone = "UTC";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public static UserSettings defaultsFor(UUID userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        return settings;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getSelectedDeckId() {
        return selectedDeckId;
    }

    public void setSelectedDeckId(UUID selectedDeckId) {
        this.selectedDeckId = selectedDeckId;
    }

    public LocalTime getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(LocalTime preferredTime) {
        this.preferredTime = preferredTime;
    }

    public int getRushSeconds() {
        return rushSeconds;
    }

    public void setRushSeconds(int rushSeconds) {
        this.rushSeconds = rushSeconds;
    }

    public int getClozeSeconds() {
        return clozeSeconds;
    }

    public void setClozeSeconds(int clozeSeconds) {
        this.clozeSeconds = clozeSeconds;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public LocalDate getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(LocalDate lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public int getBestRushScore() {
        return bestRushScore;
    }

    public void setBestRushScore(int bestRushScore) {
        this.bestRushScore = bestRushScore;
    }

    public int getMaxComboRecord() {
        return maxComboRecord;
    }

    public void setMaxComboRecord(int maxComboRecord) {
        this.maxComboRecord = maxComboRecord;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
