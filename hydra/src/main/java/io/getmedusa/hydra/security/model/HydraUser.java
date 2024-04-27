package io.getmedusa.hydra.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class HydraUser implements UserDetails {

    private Long id;
    private String encodedPassword;
    private String username;
    private String roles;
    private Map<String, String> additionalMetadata = new HashMap<>();
    private boolean accountExpired = false;
    private boolean accountLocked = false;
    private boolean credentialsExpired = false;
    private boolean enabled = true;


    public List<String> getRoles() {
        if(roles == null || roles.isBlank()) return new ArrayList<>();
        return Arrays.asList(roles.split(","));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles().stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return encodedPassword;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setAdditionalMetadata(Map<String, String> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getAdditionalMetadata() {
        return additionalMetadata;
    }

    public Long getId() {
        return id;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }
}