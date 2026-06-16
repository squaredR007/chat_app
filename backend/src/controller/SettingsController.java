package controller;

import service.SettingsService;

public class SettingsController {
    private SettingsService settingsService; //access to the service


    //constructor
    public SettingsController(SettingsService settingsService){
        this.settingsService=settingsService;
    }


    //account change handle
    public boolean changeNumber(String userId, String number){
        return settingsService.changeNumber(userId, number);
    }

    public boolean changeUsername(String userId, String username){
        return settingsService.changeUsername(userId, username);
    }

    public boolean changeBackground(String userId, String background){
        return settingsService.changeBackground(userId, background);
    }

    public boolean deleteAccount(String userId){
        return settingsService.deletedAccount(userId);
    }


    //profile change handle
    public boolean changeProfileImage(String userId, String profileImage){
        return settingsService.changeProfileImage(userId, profileImage);
    }

    public boolean changeDisplayName(String userId, String newName){
        return settingsService.changeDisplayName(userId, newName);
    }

    public boolean changeBiography(String userId, String biography){
        return settingsService.changeBiography(userId, biography);
    }


    //chat change handle
    public boolean changeDarkMode(String userId, boolean darkmode){
        return settingsService.changeDarkMode(userId, darkmode);
    }

}
