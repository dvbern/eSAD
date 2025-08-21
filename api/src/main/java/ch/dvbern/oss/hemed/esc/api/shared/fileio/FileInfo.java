/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.io.Serial;
import java.io.Serializable;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileNameConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.tika.mime.MediaType;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@Embeddable
@ToString
public abstract class FileInfo implements Serializable {

	@Serial
    private static final long serialVersionUID = -1805105243056027213L;

	@Valid
	@Column(nullable = false)
	@Convert(converter = CleanFileNameConverter.class)
	private CleanFileName fileName = CleanFileName.parse("filename-not-set.txt");

    @Column(nullable = false)
	private long fileSize = 0;

	@NotNull
	@Column(nullable = false)
	@Convert(converter = MediaTypeConverter.class)
	@Valid
    private MediaType mediaType;


	public boolean isPresent() {
		return fileSize > 0;
	}
}
