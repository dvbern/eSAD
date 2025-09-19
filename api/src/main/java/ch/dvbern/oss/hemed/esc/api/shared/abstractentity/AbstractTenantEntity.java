package ch.dvbern.oss.hemed.esc.api.shared.abstractentity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@MappedSuperclass
// Envers might interfere with liquibase diff: EJB3-Extension
@Getter
public abstract class AbstractTenantEntity<T extends AbstractTenantEntity<T>> extends AbstractEntity<T> {

    private static final long serialVersionUID = -7078681254391207481L;

    @Column(nullable = false, updatable = false)
    private String tenant = "DEFAULT"; // we switched to database multitenancy


    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append(getId())
            .append(getTenant());
    }
}
