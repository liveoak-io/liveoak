package io.liveoak.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.SynchronousResource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PagingLinksBuilder {

    private final RequestContext ctx;
    private int count = -1;
    private int totalCount = -1;
    private URI uri;

    public PagingLinksBuilder(RequestContext ctx) {
        this.ctx = ctx;
    }

    public PagingLinksBuilder uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public PagingLinksBuilder count(int count) {
        this.count = count;
        return this;
    }

    public PagingLinksBuilder totalCount(int count) {
        this.totalCount = count;
        return this;
    }

    public List<SynchronousResource> build() {
        try {
            return generatePagingLinks();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to encode uri: ", e);
        }
    }

    protected List<SynchronousResource> generatePagingLinks() throws URISyntaxException {

        UriBuilder uriBuilder = new UriBuilder(uri);
        LinkedList<SynchronousResource> links = new LinkedList<>();

        MapResource link;
        for (String name: ctx.resourceParams().names()) {
            if ("offset".equals(name) || "limit".equals(name)) {
                continue;
            }
            List<String> values = ctx.resourceParams().values(name);
            for (String value: values) {
                uriBuilder.addParam(name, value);
            }
        }
        int offset = ctx.pagination().offset();
        int limit = ctx.pagination().limit();

        int lastPageOffset = 0;
        if (totalCount > 0 & limit != 0) {
            int mod = totalCount % limit;
            lastPageOffset = totalCount - (mod == 0 ? limit : mod);
        }

        // add first / prev links if current 'page' is not the first page
        if (offset > 0) {
            link = new MapResource();
            link.put("rel", "first");
            link.put(LiveOak.HREF, uriBuilder.copy()
                    .addParam("limit", limit)
                    .build());
            links.add(link);

            int prev = offset - limit;
            if (totalCount != -1) {
                prev = prev > lastPageOffset ? lastPageOffset : prev;
            }
            prev = prev < 0 ? 0 : prev;

            link = new MapResource();
            link.put("rel", "prev");
            link.put(LiveOak.HREF, uriBuilder.copy()
                    .addParamIf(prev > 0, "offset", prev)
                    .addParam("limit", limit)
                    .build());
            links.add(link);
        }

        // add next / last links if current 'page' is not the last page
        // When totalCount is not known, and the number of all results
        // is equal to limit() we still add links to next (empty) page
        if ((totalCount != -1 && offset < lastPageOffset)
                || (totalCount == -1 && count == limit)) {

            link = new MapResource();
            link.put("rel", "next");
            link.put(LiveOak.HREF, uriBuilder.copy()
                    .addParam("offset", offset + limit)
                    .addParam("limit", limit)
                    .build());
            links.add(link);
        }

        // if we know the totalCount then we can add rel:last, otherwise skip it
        // Also set rel:last if offset points beyond the last page
        if (totalCount != -1 && offset != lastPageOffset) {
            link = new MapResource();
            link.put("rel", "last");
            link.put(LiveOak.HREF, uriBuilder.copy()
                    .addParamIf(lastPageOffset > 0, "offset", lastPageOffset)
                    .addParam("limit", limit)
                    .build());
            links.add(link);
        }
        return links;
    }
}
