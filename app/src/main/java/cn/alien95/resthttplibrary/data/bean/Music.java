package cn.alien95.resthttplibrary.data.bean;

/**
 * Created by linlongxin on 2016/5/15.
 */
public class Music {


    /**
     * albumid : 1347922
     * albummid : 000mMTtV3BTWQT
     * albumpic_big : http://i.gtimg.cn/music/photo/mid_album_300/Q/T/000mMTtV3BTWQT.jpg
     * albumpic_small : http://i.gtimg.cn/music/photo/mid_album_90/Q/T/000mMTtV3BTWQT.jpg
     * downUrl : http://tsmusic24.tc.qq.com/106097780.mp3
     * seconds : 245
     * singerid : 3954
     * singername : 汪苏泷
     * songid : 106097780
     * songname : 还给你一些孤单 ( 《我是杜拉拉》电视剧插曲)
     * url : http://ws.stream.qqmusic.qq.com/106097780.m4a?fromtag=46
     */

    private String albumpic_big;
    private String albumpic_small;
    private String downUrl;
    private int seconds;
    private String singername;
    private String songname;
    private String url;

    public String getAlbumpic_big() {
        return albumpic_big;
    }

    public void setAlbumpic_big(String albumpic_big) {
        this.albumpic_big = albumpic_big;
    }

    public String getAlbumpic_small() {
        return albumpic_small;
    }

    public void setAlbumpic_small(String albumpic_small) {
        this.albumpic_small = albumpic_small;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getSingername() {
        return singername;
    }

    public void setSingername(String singername) {
        this.singername = singername;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Music{" +
                "albumpic_big='" + albumpic_big + '\'' +
                ", albumpic_small='" + albumpic_small + '\'' +
                ", downUrl='" + downUrl + '\'' +
                ", seconds=" + seconds +
                ", singername='" + singername + '\'' +
                ", songname='" + songname + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
