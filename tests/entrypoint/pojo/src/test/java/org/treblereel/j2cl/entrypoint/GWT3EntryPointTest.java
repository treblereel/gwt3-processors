package org.treblereel.j2cl.entrypoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.nio.file.Path;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class GWT3EntryPointTest {

  @Test
  public void testOne() throws MalformedURLException {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless", "--window-size=1920,1200");

    ChromeDriver driver = new ChromeDriver(options);
    Path path = Path.of("target", "j2cl", "launcherDir", "index.html");

    driver.get(path.toUri().toURL().toString());
    assertEquals("J2CL", driver.getTitle());
    Boolean result = (Boolean) driver.executeScript("return window.started");
    assertTrue(result);
  }
}
