package kr.or.hanium.chungbukhansung.escapepresbyopia.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Meta {
    public class Text {
        class Vertex { int x, y; }

        public String text;

        /**
         * 좌하단 - 우하단 - 우상단 - 좌상단 순
         */
        public List<Vertex> box;
    }

    public class Audio {
        public int time;
        public String type;
        public int start, end;
        public String value;
    }

    private Text text;
    private Audio audio;

    public static List<Meta> createMetas(String textMeta, String audioMeta) {
        String[] lines = audioMeta.split("\n");
        if (lines.length < 1) return new ArrayList<>();

        List<Meta> metas = new ArrayList<>();
        for (String line : lines) {
            Meta.Audio audioMetaItem = new Gson().fromJson(line, Meta.Audio.class);
            metas.add(new Meta(null, audioMetaItem));
        }

        lines = textMeta.split("\n");
        if (lines.length < 1) return new ArrayList<>();

        int i = 0;
        for (String line : lines) {
            Meta.Text textMetaItem = new Gson().fromJson(line, Meta.Text.class);
            Meta meta;
            int j = i;
            while (true) {
                if (i == metas.size()) {
                    i = j;
                    break;
                }

                meta = metas.get(i++);

                if (meta.getText() == null && textMetaItem.text.equals(meta.getAudio().value)) {
                    meta.setText(textMetaItem);
                    break;
                }
            }
        }

        for (int k = metas.size() - 1; k >= 0; k--) {
            Meta meta = metas.get(k);
            if (meta.text == null)
                metas.remove(meta);
        }

        return metas;
    }

    private Meta(Text text, Audio audio) {
        this.text = text;
        this.audio = audio;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }
}
