package com.mitchbarnett.wikibanktagintegration;

import com.mitchbarnett.wikibanktagintegration.WikiBankTagIntegrationPlugin;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

public class WikiBankTagQueryTest {

    private WikiBankTagIntegrationPlugin plugin;

    @Before
    public void initializeTestEnvironment() throws ReflectiveOperationException {
        this.plugin = new WikiBankTagIntegrationPlugin();
        Field httpClient = WikiBankTagIntegrationPlugin.class.getDeclaredField("httpClient");
        httpClient.setAccessible(true);
        httpClient.set(this.plugin, new OkHttpClient());
    }

    @Test
    public void testQueryByCategory() {
        Assert.assertTrue(
                "Failed to query by category",
                plugin.getCategoryIDs("ores").length > 0);
        Assert.assertTrue(
                "Failed to query by category",
                plugin.getCategoryIDs("Members' items").length > 0);
        Assert.assertTrue(
                "Failed to query by category",
                plugin.getCategoryIDs("Non-GE items").length > 0);
    }

    @Test
    public void testQueryByMonster() {
        Assert.assertTrue(
                "Failed to query by monster",
                plugin.getDropIDs("imp").length > 0);
        Assert.assertTrue(
                "Failed to query by monster",
                plugin.getDropIDs("Rune dragon").length > 0);
        Assert.assertTrue(
                "Failed to query by monster",
                plugin.getDropIDs("Rune_dragon").length > 0);
    }

}