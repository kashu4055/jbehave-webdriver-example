package org.jbehave.tutorials.etsy;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.configuration.groovy.GroovyResourceFinder;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;
import org.jbehave.web.selenium.ContextView;
import org.jbehave.web.selenium.LocalFrameContextView;
import org.jbehave.web.selenium.PerStoriesWebDriverSteps;
import org.jbehave.web.selenium.SeleniumConfiguration;
import org.jbehave.web.selenium.SeleniumContext;
import org.jbehave.web.selenium.SeleniumContextOutput;
import org.jbehave.web.selenium.SeleniumStepMonitor;
import org.jbehave.web.selenium.TypeWebDriverProvider;
import org.jbehave.web.selenium.WebDriverProvider;
import org.jbehave.web.selenium.WebDriverScreenshotOnFailure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

public class EtsyDotComStories extends JUnitStories {

    private WebDriverProvider driverProvider = new TypeWebDriverProvider();
    private Configuration configuration;
    private static ContextView contextView = new LocalFrameContextView().sized(640,120);
    private static SeleniumContext seleniumContext = new SeleniumContext();

    static {
        System.setProperty("webdriver.firefox.profile", "WebDriver");
    }

    @Override
    public Configuration configuration() {
        configuration = makeConfiguration(this.getClass(), driverProvider);
        return configuration;
    }

    public static Configuration makeConfiguration(Class<?> embeddableClass, WebDriverProvider driverProvider) {

        return new SeleniumConfiguration()
            .useWebDriverProvider(driverProvider)
            .useSeleniumContext(seleniumContext)
            .useFailureStrategy(new FailingUponPendingStep())
            .useStepMonitor(new SeleniumStepMonitor(contextView, new SeleniumContext(), new SilentStepMonitor()))
            .useStoryLoader(new LoadFromClasspath(embeddableClass.getClassLoader()))
            .useStoryReporterBuilder(
                new StoryReporterBuilder()
                    .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                    .withDefaultFormats()
                    .withFormats(new SeleniumContextOutput(seleniumContext), CONSOLE, TXT, HTML, XML));
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> steps = makeGroovyCandidateSteps(configuration(), new GroovyResourceFinder(), driverProvider);
        steps.add(0, stepify(new PerStoriesWebDriverSteps(driverProvider))); // before other Groovy steps
        steps.add(stepify(new WebDriverScreenshotOnFailure(driverProvider)));

        return steps;
    }

    private Steps stepify(final Object steps) {
        return new Steps(configuration, steps);
    }

    public static List<CandidateSteps> makeGroovyCandidateSteps(final Configuration configuration, GroovyResourceFinder resourceFinder, final WebDriverProvider webDriverProvider) {

        GroovyContext context = new GroovyContext(resourceFinder) {
            @Override
            public Object newInstance(Class<?> parsedClass) {
                if (parsedClass.getName().contains("pages.")) {
                    return new Object();
                }
                try {
                    Object inst = null;
                    try {
                        inst = parsedClass.newInstance();
                        Method declaredMethod = parsedClass.getDeclaredMethod("setWebDriverProvider", WebDriverProvider.class);
                        declaredMethod.invoke(inst, webDriverProvider);
                    } catch (NoSuchMethodException e) {
                        // fine, it does not need a WebDriverProvider via setter.
                    }
                    return inst;
                } catch (IllegalAccessException e) {
                    return ""; // not a steps class, discard for the sake of steps registration
                } catch (InvocationTargetException e) {
                    return ""; // not a steps class, discard for the sake of steps registration
                } catch (InstantiationException e) {
                    return ""; // not a steps class, discard for the sake of steps registration
                }
            }
        };

        return new GroovyStepsFactory(configuration, context).createCandidateSteps();
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass())
                        .getFile(), asList("**/*.story"), null);
    }

    /**
     * This could go into JBehave-Web perhaps.
     */

    @Override
    public void run() throws Throwable {
        try {
            super.run();
        } finally {
            contextView.close();
        }
    }

}
