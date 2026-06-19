package cwlib.types.swing;

import javax.swing.tree.DefaultMutableTreeNode;

import cwlib.structs.profile.InventoryItem;

public class SaveDataTreeNode extends DefaultMutableTreeNode 
{
    private final InventoryItem _item;

    public SaveDataTreeNode(InventoryItem item)
    {
        super(item != null ? item.details.userCreatedDetails != null ? item.details.userCreatedDetails.name : "Some kind of object" : "<ROOT>");
        _item = item;
    }
    
    public String getName()
    {
        return (String)userObject;
    }

    public InventoryItem getItem()
    {
        return _item;
    }
}
