package org.projectodd.restafari.container;

import java.util.StringTokenizer;


public class ResourcePath {

    public ResourcePath(String path) {
        this.fullPath = path;
        
        StringTokenizer tokenizer = new StringTokenizer(this.fullPath, "/");
        
        if ( tokenizer.hasMoreTokens() ) {
            this.type = tokenizer.nextToken();
        }
        
        if ( tokenizer.hasMoreTokens() ) {
            this.collectionName = tokenizer.nextToken();
        }
        
        if ( tokenizer.hasMoreTokens() ) {
            this.resourceId = tokenizer.nextToken();
        }
        
        if ( tokenizer.hasMoreTokens() ) {
            this.propertyName = tokenizer.nextToken();
        }
    }
    
    public String getFullPath() {
        return this.fullPath;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public String getResourceId() {
        return this.resourceId;
    }
    
    public String getPropertyName() {
        return this.propertyName;
    }
    
    public boolean isTypePath() {
        return this.type != null && this.collectionName == null;
    }
    
    public boolean isCollectionPath() {
        return this.collectionName != null && this.resourceId == null;
    }
    
    public boolean isResourcePath() {
        return this.resourceId != null && this.propertyName == null;
    }
    
    public boolean isResourcePropertyPath() {
        return this.propertyName != null;
    }

    private String fullPath;
    
    private String type;
    private String collectionName;
    private String resourceId;
    private String propertyName;


}
