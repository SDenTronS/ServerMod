package com.dentron.servermod.teams;

import com.dentron.servermod.utils.ModConstants;

public class ModPlayerStatsHandler {
    public byte teamID;
    public byte lives = ModConstants.PLAYER_LIVES;

    public byte getTeamID() {
        return this.teamID;
    }

    public void setTeamID(byte teamID) {
        this.teamID = teamID;
    }


    public byte getLives() {
        return this.lives;
    }

    public void setLives(byte value) {
        this.lives = value;
    }

    public void reduceLives() {
        this.lives--;
    }

    public boolean is_lives_over(){
        return this.lives <= 0;
    }

    public void toDefault() {
        this.teamID = 0;
        this.lives = ModConstants.PLAYER_LIVES;
    }
}
