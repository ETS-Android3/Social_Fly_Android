package com.gautam.socialfly.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.gautam.socialfly.Fragments.ChatsFragment;
import com.gautam.socialfly.Fragments.ContactsFragment;

import com.gautam.socialfly.Fragments.RequestsFragment;

import org.jetbrains.annotations.NotNull;

public class TabAccessorAdapter extends FragmentPagerAdapter
{
    public TabAccessorAdapter(@NonNull @NotNull FragmentManager fm) {
        super(fm);
    }

    public TabAccessorAdapter(@NonNull @NotNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0 :
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 2 :
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;

        }

    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0 :
                return "Chats";

            case 1:
                return "Contacts";
            case 2 :
                return "Requests";

            default:
                return null;

        }
    }
}
