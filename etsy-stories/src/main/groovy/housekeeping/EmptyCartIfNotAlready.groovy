package housekeeping

import org.jbehave.core.annotations.BeforeScenario
import org.jbehave.web.selenium.WebDriverProvider
import org.openqa.selenium.WebDriverException

class EmptyCartIfNotAlready {

  WebDriverProvider webDriverProvider;

  @BeforeScenario
  def emptyCart() {
    try {
      webDriverProvider.get().manage().deleteCookieNamed("cart")
    } catch (WebDriverException e) {
      // tis OK
    }
  }
  


}
