package com.digitalbarista.cat.twitter.bindings;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")
public class Tweeter {
	private static SimpleDateFormat df;
	static
	{
		df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private long id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private URL profileImage;
	private URL url;
	private boolean isProtected;
	private int followerCount;
	private String profileBGColor;
	private String profileTextColor;
	private String profileLinkColor;
	private String profileSidebarFillColor;
	private String profileSidebarBorderColor;
	private int friendCount;
	private Date createdAt;
	private int favoritesCount;
	private long utcOffset;
	private String timezone;
	private URL profileBackgroundImage;
	private boolean profileBackgroundTiled;
	private int statusesCount;
	private boolean notifications;
	private boolean following;
	private boolean verified;
	
	@XmlElement(name="id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@XmlElement(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="screen_name")
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	@XmlElement(name="location")
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	@XmlElement(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name="profile_image_url")
	public URL getProfileImage() {
		return profileImage;
	}
	public void setProfileImage(URL profileImage) {
		this.profileImage = profileImage;
	}

	@XmlElement(name="url")
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}

	@XmlElement(name="protected")
	public boolean isProtected() {
		return isProtected;
	}
	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	@XmlElement(name="followers_count")
	public int getFollowerCount() {
		return followerCount;
	}
	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	@XmlElement(name="profile_background_color")
	public String getProfileBGColor() {
		return profileBGColor;
	}
	public void setProfileBGColor(String profileBGColor) {
		this.profileBGColor = profileBGColor;
	}

	@XmlElement(name="profile_text_color")
	public String getProfileTextColor() {
		return profileTextColor;
	}
	public void setProfileTextColor(String profileTextColor) {
		this.profileTextColor = profileTextColor;
	}

	@XmlElement(name="profile_link_color")
	public String getProfileLinkColor() {
		return profileLinkColor;
	}
	public void setProfileLinkColor(String profileLinkColor) {
		this.profileLinkColor = profileLinkColor;
	}

	@XmlElement(name="profile_sidebar_fill_color")
	public String getProfileSidebarFillColor() {
		return profileSidebarFillColor;
	}
	public void setProfileSidebarFillColor(String profileSidebarFillColor) {
		this.profileSidebarFillColor = profileSidebarFillColor;
	}

	@XmlElement(name="profile_sidebar_border_color")
	public String getProfileSidebarBorderColor() {
		return profileSidebarBorderColor;
	}
	public void setProfileSidebarBorderColor(String profileSidebarBorderColor) {
		this.profileSidebarBorderColor = profileSidebarBorderColor;
	}

	@XmlElement(name="friends_count")
	public int getFriendCount() {
		return friendCount;
	}
	public void setFriendCount(int friendCount) {
		this.friendCount = friendCount;
	}

	@XmlElement(name="created_at")
	public String getCreatedAt() {
		return df.format(createdAt);
	}
	public void setCreatedAt(String createdAt) {
		try
		{
			this.createdAt = df.parse(createdAt);
		}catch(Exception e){}
	}

	public Date getCreatedAtDate()
	{
		return createdAt;
	}
	public void setCreatedAtDate(Date ca)
	{
		createdAt = ca;
	}
	
	@XmlElement(name="favourites_count")
	public int getFavoritesCount() {
		return favoritesCount;
	}
	public void setFavoritesCount(int favoritesCount) {
		this.favoritesCount = favoritesCount;
	}

	@XmlElement(name="utc_offset")
	public long getUtcOffset() {
		return utcOffset;
	}
	public void setUtcOffset(long utcOffset) {
		this.utcOffset = utcOffset;
	}

	@XmlElement(name="time_zone")
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@XmlElement(name="profile_background_image_url")
	public URL getProfileBackgroundImage() {
		return profileBackgroundImage;
	}
	public void setProfileBackgroundImage(URL profileBackgroundImage) {
		this.profileBackgroundImage = profileBackgroundImage;
	}

	@XmlElement(name="profile_background_tile")
	public boolean isProfileBackgroundTiled() {
		return profileBackgroundTiled;
	}
	public void setProfileBackgroundTiled(boolean profileBackgroundTiled) {
		this.profileBackgroundTiled = profileBackgroundTiled;
	}

	@XmlElement(name="statuses_count")
	public int getStatusesCount() {
		return statusesCount;
	}
	public void setStatusesCount(int statusesCount) {
		this.statusesCount = statusesCount;
	}

	@XmlElement(name="notifications")
	public boolean isNotifications() {
		return notifications;
	}
	public void setNotifications(boolean notifications) {
		this.notifications = notifications;
	}
	
	@XmlElement(name="following")
	public boolean isFollowing() {
		return following;
	}
	public void setFollowing(boolean following) {
		this.following = following;
	}
	
	@XmlElement(name="verified")
	public boolean isVerified() {
		return verified;
	}
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
}
