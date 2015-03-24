package org.developfreedom.ccdroid.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ProjectTest {
    Project project;

    @Before
    public void setUp() throws Exception {
        project = new Project(
                "shubhamchaudhary/wordpowermadeeasy",
                "Sleeping",
                "31",
                "Success",
                "2015-03-22T11:32:14.000+0000",
                "https://travis-ci.org/shubhamchaudhary/wordpowermadeeasy"
        );

        //project.setName("shubhamchaudhary/wordpowermadeeasy");
        //project.setActivity("Sleeping");
        //project.setLastBuildStatus("Success");
        //project.setLastBuildLabel("31");
        //project.setLastBuildTime("2015-03-22T11:32:14.000+0000");
        //project.setWebUrl("https://travis-ci.org/shubhamchaudhary/wordpowermadeeasy");
    }

    @Test
    public void testNameIsString() {
        assertEquals("shubhamchaudhary/wordpowermadeeasy", project.getName());
    }

    @Test
    public void testActivityIsString() {
        assertEquals("Sleeping", project.getActivity());
    }

    @Test
    public void testLastBuildStatusIsString() {
        assertEquals("Success", project.getLastBuildStatus());
    }

    @Test
    public void testLastBuildLabelIsString() {
        assertEquals("31", project.getLastBuildLabel());
    }

    @Test
    public void testLastBuildTimeIsString() {
        assertEquals("2015-03-22T11:32:14.000+0000", project.getLastBuildTime());
    }

    @Test
    public void testWebUrlIsString() {
        assertEquals("https://travis-ci.org/shubhamchaudhary/wordpowermadeeasy", project.getWebUrl());
    }
}