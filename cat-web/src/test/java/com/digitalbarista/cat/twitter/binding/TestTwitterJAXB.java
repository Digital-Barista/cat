package com.digitalbarista.cat.twitter.binding;

import java.io.InputStream;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.DirectMessageCollection;
import com.digitalbarista.cat.twitter.bindings.IdList;
import com.digitalbarista.cat.twitter.bindings.Tweeter;

public class TestTwitterJAXB {

	@Test
	public void testTweeterClass() throws JAXBException
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/digitalbarista/cat/twitter/binding/simpleTweeter.xml");

		JAXBContext context = JAXBContext.newInstance(Tweeter.class);
		Unmarshaller decoder = context.createUnmarshaller();
		
		Tweeter tweet = (Tweeter)decoder.unmarshal(stream);
		
		verifySender(tweet);
	}
	
	@Test
	public void testDirectMessageClass() throws JAXBException
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/digitalbarista/cat/twitter/binding/simpleDirectMessage.xml");

		JAXBContext context = JAXBContext.newInstance(DirectMessage.class,Tweeter.class);
		Unmarshaller decoder = context.createUnmarshaller();
		
		DirectMessage dm = (DirectMessage)decoder.unmarshal(stream);
		verifyDM(dm);
		verifySender(dm.getSender());
		verifyReceiver(dm.getRecipient());
	}
	
	
	@Test
	public void testDirectMessagesClass() throws JAXBException
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/digitalbarista/cat/twitter/binding/simpleDirectMessages.xml");

		JAXBContext context = JAXBContext.newInstance(DirectMessageCollection.class,DirectMessage.class,Tweeter.class);
		Unmarshaller decoder = context.createUnmarshaller();
		
		DirectMessageCollection dmc = (DirectMessageCollection)decoder.unmarshal(stream);
		assert dmc!=null : "DMC is null";
		assert dmc.getDirectMessages()!=null : "Direct message collection is null.";
		assert dmc.getDirectMessages().size()==1 : "Direct message collection has the wrong number of elements.";
		verifyDM(dmc.getDirectMessages().get(0));
		verifySender(dmc.getDirectMessages().get(0).getSender());
		verifyReceiver(dmc.getDirectMessages().get(0).getRecipient());
	}
	
	@Test
	public void testIDListClass() throws JAXBException
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/digitalbarista/cat/twitter/binding/idList.xml");

		JAXBContext context = JAXBContext.newInstance(IdList.class);
		Unmarshaller decoder = context.createUnmarshaller();

		IdList idList = (IdList)decoder.unmarshal(stream);
		assert idList!=null : "ID List is null";
		assert idList.getIds()!=null : "No IDs in list";
		Assert.assertEquals(idList.getIds().size(),4,"wrong number of IDs returned");
		Assert.assertEquals(idList.getIds().get(0), new Long(1234567),"id[0] incorrect");
		Assert.assertEquals(idList.getIds().get(1), new Long(2345678),"id[1] incorrect");
		Assert.assertEquals(idList.getIds().get(2), new Long(3456789),"id[2] incorrect");
		Assert.assertEquals(idList.getIds().get(3), new Long(4567890),"id[3] incorrect");
		Assert.assertEquals(idList.getNextCursor(), new BigInteger("11112222333344445555"),"Incorrect next cursor.");
		Assert.assertEquals(idList.getPreviousCursor(), new BigInteger("55554444333322221111"),"Incorrect previous cursor.");
	}
	
	private void verifyDM(DirectMessage dm)
	{
		assert dm!=null : "No direct message built";
		Assert.assertEquals(dm.getId(), 88619848, "DM ID was incorrect.");
		Assert.assertEquals(dm.getSenderId(), 1401881, "DM Sender ID was incorrect.");
		Assert.assertEquals(dm.getText(), "all your bases are belong to us.", "DM text was incorrect.");
		Assert.assertEquals(dm.getRecipientId(), 7004322, "DM Recipient ID was incorrect.");
		Assert.assertEquals(dm.getCreatedAt(), "Wed Apr 08 20:30:04 +0000 2009", "DM created date was incorrect.");
		Assert.assertEquals(dm.getSenderScreenName(), "dougw", "DM ID was incorrect.");
		Assert.assertEquals(dm.getRecipientScreenName(), "igudo", "DM ID was incorrect.");
	}
	
	private void verifySender(Tweeter tweet)
	{
		assert tweet!=null : "No tweet built";
		Assert.assertEquals(tweet.getId(), 1401881, "Tweeter ID was incorrect.");
		Assert.assertEquals(tweet.getName(), "Doug Williams", "Tweeter Name was incorrect.");
		Assert.assertEquals(tweet.getScreenName(), "dougw", "Tweeter Screen Name was incorrect.");
		Assert.assertEquals(tweet.getLocation(), "San Francisco, CA", "Tweeter Location was incorrect.");
		Assert.assertEquals(tweet.getDescription(), "Twitter API Support. Internet, greed, users, dougw and opportunities are my passions.", "Tweeter Description was incorrect.");
		Assert.assertEquals(tweet.getProfileImage().toString(), "http://s3.amazonaws.com/twitter_production/profile_images/59648642/avatar_normal.png", "Tweeter Profile Image was incorrect.");
		Assert.assertEquals(tweet.getUrl().toString(), "http://www.igudo.com", "Tweeter URL was incorrect.");
		Assert.assertEquals(tweet.isProtected(), false, "Tweeter protected flag was incorrect.");
		Assert.assertEquals(tweet.getFollowerCount(), 1036, "Tweeter follower count was incorrect.");
		Assert.assertEquals(tweet.getProfileBGColor(), "9ae4e8", "Tweeter profile BG color was incorrect.");
		Assert.assertEquals(tweet.getProfileTextColor(), "000000", "Tweeter profile text color was incorrect.");
		Assert.assertEquals(tweet.getProfileLinkColor(), "0000ff", "Tweeter profile link color was incorrect.");
		Assert.assertEquals(tweet.getProfileSidebarFillColor(), "e0ff92", "Tweeter sidebar fill color was incorrect.");
		Assert.assertEquals(tweet.getProfileSidebarBorderColor(), "87bc44", "Tweeter sidebar border color was incorrect.");
		Assert.assertEquals(tweet.getFriendCount(), 290, "Tweeter friend count was incorrect.");
		Assert.assertEquals(tweet.getCreatedAt(), "Sun Mar 18 06:42:26 +0000 2007", "Tweeter created at date was incorrect.");
		Assert.assertEquals(tweet.getFavoritesCount(), 0, "Tweeter favorites count was incorrect.");
		Assert.assertEquals(tweet.getUtcOffset(), -18000, "Tweeter UTC offset was incorrect.");
		Assert.assertEquals(tweet.getTimezone(), "Eastern Time (US & Canada)", "Tweeter timezone was incorrect.");
		Assert.assertEquals(tweet.getProfileBackgroundImage().toString(), "http://s3.amazonaws.com/twitter_production/profile_background_images/2752608/twitter_bg_grass.jpg", "Tweeter profile Bg image was incorrect.");
		Assert.assertEquals(tweet.isProfileBackgroundTiled(), false, "Tweeter profile BG tiled flag was incorrect.");
		Assert.assertEquals(tweet.getStatusesCount(), 3394, "Tweeter statuses count was incorrect.");
		Assert.assertEquals(tweet.isNotifications(), false, "Tweeter notifications flag was incorrect.");
		Assert.assertEquals(tweet.isFollowing(), false, "Tweeter following flag was incorrect.");
		Assert.assertEquals(tweet.isVerified(), true, "Tweeter verified flag was incorrect.");
	}
	
	private void verifyReceiver(Tweeter tweet)
	{
		assert tweet!=null : "No tweet built";
		Assert.assertEquals(tweet.getId(), 7004322, "Tweeter ID was incorrect.");
		Assert.assertEquals(tweet.getName(), "Doug Williams", "Tweeter Name was incorrect.");
		Assert.assertEquals(tweet.getScreenName(), "igudo", "Tweeter Screen Name was incorrect.");
		Assert.assertEquals(tweet.getLocation(), "North Carolina", "Tweeter Location was incorrect.");
		Assert.assertEquals(tweet.getDescription(), "A character.", "Tweeter Description was incorrect.");
		Assert.assertEquals(tweet.getProfileImage().toString(), "http://s3.amazonaws.com/twitter_production/profile_images/15446222/twitter_48_48_normal.jpg", "Tweeter Profile Image was incorrect.");
		Assert.assertEquals(tweet.getUrl().toString(), "http://www.igudo.com", "Tweeter URL was incorrect.");
		Assert.assertEquals(tweet.isProtected(), false, "Tweeter protected flag was incorrect.");
		Assert.assertEquals(tweet.getFollowerCount(), 19, "Tweeter follower count was incorrect.");
		Assert.assertEquals(tweet.getProfileBGColor(), "69A1AA", "Tweeter profile BG color was incorrect.");
		Assert.assertEquals(tweet.getProfileTextColor(), "000000", "Tweeter profile text color was incorrect.");
		Assert.assertEquals(tweet.getProfileLinkColor(), "F00", "Tweeter profile link color was incorrect.");
		Assert.assertEquals(tweet.getProfileSidebarFillColor(), "ACBEC1", "Tweeter sidebar fill color was incorrect.");
		Assert.assertEquals(tweet.getProfileSidebarBorderColor(), "8A8F85", "Tweeter sidebar border color was incorrect.");
		Assert.assertEquals(tweet.getFriendCount(), 3, "Tweeter friend count was incorrect.");
		Assert.assertEquals(tweet.getCreatedAt(), "Thu Jun 21 21:16:21 +0000 2007", "Tweeter created at date was incorrect.");
		Assert.assertEquals(tweet.getFavoritesCount(), 0, "Tweeter favorites count was incorrect.");
		Assert.assertEquals(tweet.getUtcOffset(), -18000, "Tweeter UTC offset was incorrect.");
		Assert.assertEquals(tweet.getTimezone(), "Eastern Time (US & Canada)", "Tweeter timezone was incorrect.");
		Assert.assertEquals(tweet.getProfileBackgroundImage().toString(), "http://static.twitter.com/images/themes/theme1/bg.gif", "Tweeter profile Bg image was incorrect.");
		Assert.assertEquals(tweet.isProfileBackgroundTiled(), false, "Tweeter profile BG tiled flag was incorrect.");
		Assert.assertEquals(tweet.getStatusesCount(), 382, "Tweeter statuses count was incorrect.");
		Assert.assertEquals(tweet.isNotifications(), false, "Tweeter notifications flag was incorrect.");
		Assert.assertEquals(tweet.isFollowing(), true, "Tweeter following flag was incorrect.");
		Assert.assertEquals(tweet.isVerified(), true, "Tweeter verified flag was incorrect.");
	}
	
}
