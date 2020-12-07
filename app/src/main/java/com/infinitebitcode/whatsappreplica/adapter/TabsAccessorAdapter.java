package com.infinitebitcode.whatsappreplica.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.infinitebitcode.whatsappreplica.fragment.ChatsFragment;
import com.infinitebitcode.whatsappreplica.fragment.ContactsFragment;
import com.infinitebitcode.whatsappreplica.fragment.GroupsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior) {                 //behavior
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }
}


/*FragmentPagerAdapter -> behavior :
* BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT - Indicates that only the current fragment will be in the Lifecycle.State.RESUMED state. */

/*TODO : Error in Color of Highlighted text and background in Chats, Groups, Contacts*/