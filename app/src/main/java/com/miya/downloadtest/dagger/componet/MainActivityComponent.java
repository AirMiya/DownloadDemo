package com.miya.downloadtest.dagger.componet;


import com.miya.downloadtest.MainActivity;
import com.miya.downloadtest.dagger.module.MainActivityModule;

import dagger.Subcomponent;

@Subcomponent(modules = MainActivityModule.class)
public interface MainActivityComponent {
    MainActivity in(MainActivity mainActivity);
}
