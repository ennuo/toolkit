package ennuo.craftworld.types.indev;

import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.StringEntry;
import java.util.ArrayList;

public abstract class BaseProfile {
  public ArrayList<ProfileItem> inventory = new ArrayList<ProfileItem>();
  public ArrayList<StringEntry> stringCollection = new ArrayList<StringEntry>();
  public boolean fromProductionBuild = false;
}
