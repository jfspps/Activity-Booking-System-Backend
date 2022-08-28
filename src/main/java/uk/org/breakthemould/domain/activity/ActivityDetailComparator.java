package uk.org.breakthemould.domain.activity;

import java.util.Comparator;

public class ActivityDetailComparator implements Comparator<ActivityDetail> {
    @Override
    public int compare(ActivityDetail o1, ActivityDetail o2) {
        // if o1 uniqueID alphabetically proceeds o2 uniqueID
        if (o1.getActivityTemplate().getUniqueID().compareTo(o2.getActivityTemplate().getUniqueID()) == 0){
            if (o1.getId() < o2.getId()){
                return -1;
            } else if (o1.getId() > o2.getId()){
                return 1;
            } else
                return 0;
        } else if (o1.getActivityTemplate().getUniqueID().compareTo(o2.getActivityTemplate().getUniqueID()) < 0){
            return -1;
        } else if (o1.getActivityTemplate().getUniqueID().compareTo(o2.getActivityTemplate().getUniqueID()) > 0){
            return 1;
        } else
            return 0;
    }
}
