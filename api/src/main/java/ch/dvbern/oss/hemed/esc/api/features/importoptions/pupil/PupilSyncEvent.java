package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(indexes = {
    @Index(name = "ix_pupilsyncevent_file", columnList = "filedata_id, id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PupilSyncEvent extends AbstractEntity<PupilSyncEvent> {

    @Serial
    private static final long serialVersionUID = 2994215971992721118L;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_pupilsyncevent_filedata"), nullable = false, updatable = false)
    private FileData fileData; // relation to the uploaded file in s3

}
