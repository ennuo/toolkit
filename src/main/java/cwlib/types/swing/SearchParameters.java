package cwlib.types.swing;

import cwlib.enums.ResourceType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.Strings;

/**
 * Toolkit search filtering settings
 */
public class SearchParameters {
    /**
     * Path to search for
     */
    private final String path;

    /**
     * Parsed resource reference from search query
     */
    private final ResourceDescriptor resource;

    /**
     * Constructs search parameters from a query
     * @param query Search query
     */
    public SearchParameters(String query) {
        this.path = query.toLowerCase().replaceAll("\\s", "");
        if (query.startsWith("res:")) {
            query = query.substring(4);
            if (Strings.isGUID(query) || Strings.isSHA1(query))
                this.resource = new ResourceDescriptor(query, ResourceType.INVALID);
            else
                this.resource = null;
        } else this.resource = null;
    }

    public String getPath() { return this.path; }
    public ResourceDescriptor getResource() { return this.resource; }
}
