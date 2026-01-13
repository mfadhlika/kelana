package com.fadhlika.kelana.dto.owntracks;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("lwt")
public record Lwt(int tst) implements Message {
}
