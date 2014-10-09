package io.liveoak.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import io.liveoak.common.DefaultPagination;
import io.liveoak.common.DefaultRequestContext;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.resource.SynchronousResource;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PagingLinksTest {

    @Test
    public void testWithTotalCount() throws Exception {

        URI uri = uri();
        ResourceParams params = params();
        Sorting sorting = sorting();

        int resultCount = 10;
        int totalCount = 35;

        Pagination pagination = new DefaultPagination(0, 10);
        RequestContext ctx = createContext(pagination, params, sorting);
        List<SynchronousResource> links = buildLinks(ctx, uri, resultCount, totalCount);


        assertThat(links.size()).isEqualTo(2);
        Map<String, ?> props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=10&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");


        // next page
        pagination = new DefaultPagination(10, 10);

        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);


        assertThat(links.size()).isEqualTo(4);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");

        props = links.get(3).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");


        // third page

        pagination = new DefaultPagination(20, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);


        assertThat(links.size()).isEqualTo(4);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=10&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");

        props = links.get(3).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");


        // last page
        resultCount = 5;
        pagination = new DefaultPagination(30, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);


        assertThat(links.size()).isEqualTo(2);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");


        // page after last
        resultCount = 0;
        pagination = new DefaultPagination(40, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);


        assertThat(links.size()).isEqualTo(3);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=30&limit=10");


        //
        // all results on one page
        //

        resultCount = 25;
        totalCount = 25;

        pagination = new DefaultPagination(0, 100);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(0);
    }

    @Test
    public void testWithTotalCountBeingMultipleOfPageSize() throws Exception {

        //
        // test totalCount being a multiple of page size (limit)
        //

        URI uri = uri();
        ResourceParams params = params();
        Sorting sorting = sorting();

        int resultCount = 10;
        int totalCount = 30;

        // second page

        Pagination pagination = new DefaultPagination(10, 10);

        RequestContext ctx = createContext(pagination, params, sorting);
        List<SynchronousResource> links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(4);
        Map<String, ?> props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");

        props = links.get(3).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");


        // last page

        pagination = new DefaultPagination(20, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(2);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=10&limit=10");


        // beyond last page
        resultCount = 0;
        pagination = new DefaultPagination(100, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(3);
        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("last");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");
    }

    @Test
    public void testWithoutTotalCount() throws Exception {

        //
        // test totalCount being a multiple of page size (limit)
        //

        URI uri = uri();
        ResourceParams params = params();
        Sorting sorting = sorting();

        int totalCount = -1;
        int resultCount = 25;

        // second page

        Pagination pagination = new DefaultPagination(0, 100);

        RequestContext ctx = createContext(pagination, params, sorting);
        List<SynchronousResource> links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(0);


        resultCount = 10;

        // first page

        pagination = new DefaultPagination(0, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(1);
        Map<String, ?> props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=10&limit=10");


        // second page

        pagination = new DefaultPagination(10, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(3);

        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(2).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("next");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");


        // third page
        resultCount = 5;

        pagination = new DefaultPagination(20, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(2);

        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=10&limit=10");

        // beyond last page
        resultCount = 0;
        pagination = new DefaultPagination(30, 10);
        ctx = createContext(pagination, params, sorting);
        links = buildLinks(ctx, uri, resultCount, totalCount);

        assertThat(links.size()).isEqualTo(2);

        props = links.get(0).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("first");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&limit=10");

        props = links.get(1).properties(ctx);
        assertThat(props.get("rel")).isEqualTo("prev");
        assertThat(props.get("href").toString()).isEqualTo(uri + "&offset=20&limit=10");
    }



    protected URI uri() throws URISyntaxException {
        return new URI("/testApp/service/collection?q=%7Buser.name%3A%27John%27%7D&sort=-lastName&xyz=custom");
    }

    protected ResourceParams params() {
        return new DefaultResourceParams.Builder()
                .add("q", "{user.name:'John'}")
                .add("sort", "-lastName")
                .add("xyz", "custom")
                .build();
    }

    protected Sorting sorting() {
        return new Sorting.Builder()
                .addSpec("lastName", false)
                .build();
    }

    protected List<SynchronousResource> buildLinks(RequestContext ctx, URI uri, int resultCount, int totalCount) {
        PagingLinksBuilder linksBuilder = new PagingLinksBuilder(ctx)
                .uri(uri)
                .count(resultCount);

        if (totalCount != -1) {
            linksBuilder.totalCount(totalCount);
        }

        return linksBuilder.build();
    }

    protected RequestContext createContext(Pagination pagination, ResourceParams params, Sorting sorting) {
        return new DefaultRequestContext.Builder()
                .pagination(pagination)
                .resourceParams(params)
                .sorting(sorting)
                .build();
    }
}
