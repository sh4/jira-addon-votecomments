package it.info.renjithv.votecomments;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{
    private final ApplicationProperties applicationProperties;

    public MyComponentWiredTest(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Test
    public void testMyName()
    {
    }
}