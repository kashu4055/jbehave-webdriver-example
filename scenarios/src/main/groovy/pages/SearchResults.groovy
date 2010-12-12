package pages

import org.openqa.selenium.By
import org.jbehave.web.selenium.WebDriverProvider
import static org.junit.Assert.fail
import org.openqa.selenium.WebElement

class SearchResults extends BasePage{

  private elems;

  def SearchResults(WebDriverProvider webDriverProvider) {
    super(webDriverProvider)
  }


  def buyFirst(String thing) {
    getElems()
    for (int i = 0; i < elems.size(); i++) {
      def elem = elems.get(i)
      def title = elem.getAttribute("title")
      if (title.contains(thing)) {
        elem.click()
        findElements(By.xpath("//input[@value = 'Add to Cart']")).get(0).click()
        return
      }
    }
    fail("no $thing in search results")
  }

  private List<WebElement> getElems() {
    elems = findElements(By.xpath("//a[@class = 'listing-thumb']"))
  }

  def someResults() {
    getElems()
    elems.shouldNotBe 0
  }
}
