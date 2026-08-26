package ru.kantser.elephantmusic.service.tag;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.TagFields;

import java.nio.file.Path;

public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    public boolean isMp3(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".mp3");
    }

    public byte[] readCoverArt(Path path) {
        try {
            AudioFile audioFile = AudioFileIO.read(path.toFile());
            if (audioFile instanceof MP3File mp3) {
                Tag tag = mp3.getID3v2Tag();
                if (tag == null) {
                    tag = mp3.getTag();
                }
                if (tag != null) {
                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null && artwork.getBinaryData() != null && artwork.getBinaryData().length > 0) {
                        return artwork.getBinaryData();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Не удалось прочитать обложку: {}", e.getMessage());
        }
        return null;
    }

    public TagFields readV1(Path path) {
        TagFields fields = new TagFields();
        try {
            AudioFile audioFile = AudioFileIO.read(path.toFile());
            if (audioFile instanceof MP3File mp3) {
                ID3v1Tag tag = mp3.getID3v1Tag();
                if (tag != null) {
                    fields.setTitle(tag.getFirst(FieldKey.TITLE));
                    fields.setArtist(tag.getFirst(FieldKey.ARTIST));
                    fields.setAlbum(tag.getFirst(FieldKey.ALBUM));
                    fields.setYear(tag.getFirst(FieldKey.YEAR));
                    fields.setComment(tag.getFirst(FieldKey.COMMENT));
                    fields.setGenre(tag.getFirst(FieldKey.GENRE));
                }
            }
        } catch (Exception e) {
            logger.warn("Не удалось прочитать ID3v1: {}", e.getMessage());
        }
        return fields;
    }

    public TagFields readV2(Path path) {
        TagFields fields = new TagFields();
        try {
            AudioFile audioFile = AudioFileIO.read(path.toFile());
            if (audioFile instanceof MP3File mp3) {
                Tag tag = mp3.getID3v2Tag();
                if (tag == null) {
                    tag = mp3.getTag();
                }
                if (tag != null) {
                    fields.setTitle(tag.getFirst(FieldKey.TITLE));
                    fields.setArtist(tag.getFirst(FieldKey.ARTIST));
                    fields.setAlbum(tag.getFirst(FieldKey.ALBUM));
                    fields.setYear(tag.getFirst(FieldKey.YEAR));
                    fields.setTrack(tag.getFirst(FieldKey.TRACK));
                    fields.setComment(tag.getFirst(FieldKey.COMMENT));
                    fields.setGenre(tag.getFirst(FieldKey.GENRE));
                    fields.setComposer(tag.getFirst(FieldKey.COMPOSER));
                }
            }
        } catch (Exception e) {
            logger.warn("Не удалось прочитать ID3v2: {}", e.getMessage());
        }
        return fields;
    }

    public void write(Path path, TagFields v1, TagFields v2) throws Exception {
        AudioFile audioFile = AudioFileIO.read(path.toFile());
        if (!(audioFile instanceof MP3File mp3)) {
            throw new IllegalStateException("Редактирование ID3 тегов доступно только для MP3 файлов");
        }

        if (v1 != null) {
            ID3v1Tag tag = mp3.getID3v1Tag();
            if (tag == null) {
                tag = new ID3v1Tag();
            }
            setSafe(tag, FieldKey.TITLE, v1.getTitle());
            setSafe(tag, FieldKey.ARTIST, v1.getArtist());
            setSafe(tag, FieldKey.ALBUM, v1.getAlbum());
            setSafe(tag, FieldKey.YEAR, v1.getYear());
            setSafe(tag, FieldKey.COMMENT, v1.getComment());
            setSafe(tag, FieldKey.GENRE, v1.getGenre());
            mp3.setID3v1Tag(tag);
        }

        if (v2 != null) {
            AbstractID3v2Tag tag = mp3.getID3v2Tag();
            if (tag == null) {
                tag = new ID3v23Tag();
            }
            setSafe(tag, FieldKey.TITLE, v2.getTitle());
            setSafe(tag, FieldKey.ARTIST, v2.getArtist());
            setSafe(tag, FieldKey.ALBUM, v2.getAlbum());
            setSafe(tag, FieldKey.YEAR, v2.getYear());
            setSafe(tag, FieldKey.TRACK, v2.getTrack());
            setSafe(tag, FieldKey.COMMENT, v2.getComment());
            setSafe(tag, FieldKey.GENRE, v2.getGenre());
            setSafe(tag, FieldKey.COMPOSER, v2.getComposer());
            mp3.setID3v2Tag(tag);
        }

        mp3.commit();
    }

    private void setSafe(Tag tag, FieldKey key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            tag.setField(key, value.trim());
        } catch (Exception e) {
            logger.warn("Не удалось записать поле {}: {}", key, e.getMessage());
        }
    }
}
