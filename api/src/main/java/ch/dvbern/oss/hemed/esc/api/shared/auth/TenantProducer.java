package ch.dvbern.oss.hemed.esc.api.shared.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import lombok.Setter;

@RequestScoped
public class TenantProducer {

    @Setter
    private TenantEnum currentTenant;


    @Produces
    @RequestScoped
    public TenantEnum produceTenant() {
        return currentTenant;
    }
}
