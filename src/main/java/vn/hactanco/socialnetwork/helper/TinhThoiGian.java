package vn.hactanco.socialnetwork.helper;

import java.time.Duration;
import java.time.Instant;

public class TinhThoiGian {
	public static String timeAgo(Instant createdAt) {

		Duration duration = Duration.between(createdAt, Instant.now());

		long seconds = duration.getSeconds();

		if (seconds < 60) {
			return seconds + "s";
		}

		long minutes = seconds / 60;
		if (minutes < 60) {
			return minutes + "p";
		}

		long hours = minutes / 60;
		if (hours < 24) {
			return hours + "h";
		}

		long days = hours / 24;
		if (days < 7) {
			return days + "d";
		}

		long weeks = days / 7;
		if (weeks < 4) {
			return weeks + "w";
		}

		long months = days / 30;
		if (months < 12) {
			return months + "m";
		}

		long years = days / 365;
		return years + "y";
	}
}
