package it.unibo.oop.lab.streams;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return this.songs.stream().map(Song::getSongName).sorted();
    }

    @Override
    public Stream<String> albumNames() {
        return this.albums.keySet().stream();
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return this.albums
            .entrySet()         //non divide a metà ma ho coppie
            .stream()
            .filter(a -> a.getValue() == year)
            .map(Entry::getKey); //a -> a.getKey()
    }

    @Override
    public int countSongs(final String albumName) {
        return this.songs
            .stream()
            .filter(s -> s.getAlbumName().isPresent())
            .filter(s -> s.getAlbumName().get().equals(albumName))
            .mapToInt(t -> 1)
            .sum();
    }

    @Override
    public int countSongsInNoAlbum() {
        return this.songs
            .stream()
            .filter(s -> s.getAlbumName().isEmpty())
            .mapToInt(t -> 1)
            .sum();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        return this.songs
            .stream()
            .filter(s -> s.getAlbumName().filter(t -> t.equals(albumName)).isPresent())
            .mapToDouble(Song::getDuration) //s -> s.getDuration()
            .average();
    }

    @Override
    public Optional<String> longestSong() {
        return this.songs
            .stream()
            //.max((a,b) -> Double.compare(a.getDuration(), b.getDuration()))
            .max(Comparator.comparingDouble(Song::getDuration))
            .map(Song::getSongName); 
    }

    @Override
    public Optional<String> longestAlbum() {
        return this.songs
            .stream()       //apro stream
            .filter(s -> s.getAlbumName().isPresent())      //filtro se album è specificato è presente
            .collect(Collectors.groupingBy(Song::getAlbumName, Collectors.summingDouble(Song::getDuration)))    //raggruppo per ogni album (dato il suo nome) tutte le durate delle canzoni di quell'album
            .entrySet().stream()       //collect restituisce una mappa, conclusiva, quindi se voglio trasformare ancora devo riaprirlo
            //gruopingby restituisce una mappa -> devo riaprirlo con entrySet lista oggetti entry che contengono chiave valore
            //keySet -> set di chiavi
            //value -> valori
            .max(Comparator.comparingDouble(Entry::getValue))   //computare al max passandogli il comparatore
            .flatMap(Entry::getKey);        //prendo la chiave ma spacchetta il contenuto, non restitusico il valore ma lo spacchetto --> prendo contenuto dell'optional
        //prendo singolarmente il contenuto dell'optional
        //verifico implicitamente se è vuoto perché restituisce un eccezione quindi con fmap restituisce un opzionbale vuoto
            //.get().getKey();      //la max, potrebbe restituire un vuoto
        }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
