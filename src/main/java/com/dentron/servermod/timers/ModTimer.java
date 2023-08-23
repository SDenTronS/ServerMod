package com.dentron.servermod.timers;

public class ModTimer {
    private Integer ticks;

    public ModTimer(Integer ticks){
        this.ticks = ticks;
    }

    public void tick(){
        this.ticks--;
    }
    public void tick(Integer multiply){
        this.ticks -= multiply;
    }

    public Integer getTicks(){
        return ticks;
    }

//    private String two_sign_format(Integer num){
//        String text = String.valueOf(num);
//        int text_len = text.length();
//
//        if (text_len % 2 == 0){
//            return text;
//        }
//        else
//            return "0" + text;
//    }

    @Override
    public String toString() {
        Integer seconds = ticks % 60;
        Integer minutes = (ticks / 60) % 60;
        Integer hours = ticks / (60 * 60);

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public void setTicks(Integer ticks){
        this.ticks = ticks;
    }

    public boolean is_times_up(){
        return (ticks == 0);
    }
}
