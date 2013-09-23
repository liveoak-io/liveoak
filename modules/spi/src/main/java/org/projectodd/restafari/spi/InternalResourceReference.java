package org.projectodd.restafari.spi;

public class InternalResourceReference {
    

    public InternalResourceReference(String collectionName) {
        this( collectionName, null );
    }
    
    public InternalResourceReference(String collectionName, String id) {
        this.collectionName = collectionName;
        this.id = id;
    }

    private String collectionName;
    private String id;
}
