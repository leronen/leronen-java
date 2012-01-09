package util.converter;

public class NanosToMillisConverter implements Converter<Long,Long> {
    public Long convert(Long pVal) {
        return (long)pVal/1000000;
    }

}
