package fr.cnrs.iremus.sherlock.pojo.user.config;

import io.micronaut.core.annotation.Introspected;

@UserConfigValidator
@Introspected
public class UserConfigEdit {
    @UserEmojiValidator
    private String emoji;
    @UserColorValidator
    private String color;

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
