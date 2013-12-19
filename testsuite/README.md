Running tests
=============

By default the testsuite starts a LiveOak instance, and uses the PhantomJS for browser testing.


Browser
-------

You can use either PhantomJS (phantomjs), Chrome (chrome) or Firefox (firefox) for browser testing by setting the `browser` system property. For example:

    mvn test -Dbrowser=firefox

Chrome and PhantomJS requires platform specific binaries. PhantomJS binaries are downloaded by Maven so you don't need to do anything to use it. To use Chrome you need to download the Chrome binaries (https://code.google.com/p/selenium/wiki/ChromeDriver).

LiveOak output
--------------

By default the output produced by LiveOak is not shown, to display it run the tests with:

    mvn test -Dliveoak.showOutput=true

Remote
------

You can manually start LiveOak if you want. You have to start it within `testsuite/src/app` as the tests depends on this. Once started run the tests with:

    mvn test -Dliveoak.remote=true

You can also specific the base url:

    mvn test -Dliveoak.remote=true -Dliveoak.url=http://liveoak:8081
    

Createing tests
===============

Using the JUnit runner will start/stop a LiveOak instance for the test:

    @RunWith(LiveOak.class)

It's also possible to inject a range of resources into the test:

    @Resource
    private URL url;

    @Resource
    private HttpClient client;

    @Resource
    private WebDriver driver;

    @Resource
    private JsExecutor js;
