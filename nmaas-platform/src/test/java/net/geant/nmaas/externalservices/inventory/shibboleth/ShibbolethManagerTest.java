package net.geant.nmaas.externalservices.inventory.shibboleth;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.geant.nmaas.externalservices.inventory.gitlab.model.ShibbolethView;
import net.geant.nmaas.externalservices.inventory.shibboleth.entities.Shibboleth;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.OnlyOneShibbolethConfigSupportedException;
import net.geant.nmaas.externalservices.inventory.shibboleth.exceptions.ShibbolethConfigNotFoundException;
import net.geant.nmaas.externalservices.inventory.shibboleth.repositories.ShibbolethRepository;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

public class ShibbolethManagerTest {
    private ShibbolethRepository repo = mock(ShibbolethRepository.class);

    private ModelMapper modelMapper = new ModelMapper();

    private ShibbolethManager shibbolethManager;

    private String path;

    @Before
    public void setup(){
        shibbolethManager = new ShibbolethManager(repo, modelMapper);
        path = getClass().getClassLoader().getResource("shibboleth-key.json").getPath();
        Shibboleth shibboleth = new Shibboleth(1L, "login-url", "logout-url", path,10);
        when(repo.findAll()).thenReturn(Arrays.asList(shibboleth));
        when(repo.findById(1L)).thenReturn(Optional.of(shibboleth));
        when(repo.findById(2L)).thenReturn(Optional.empty());
        when(repo.count()).thenReturn(1L);
    }

    @Test
    public void shouldGetListOfShibbolethConfigs(){
        List<ShibbolethView> shibbolethViewList = this.shibbolethManager.getAllShibbolethConfig();
        assertThat("Size of shibboleth config list mismatch",shibbolethViewList.size() == 1);
    }

    @Test
    public void shouldGetConfigById(){
        Long configId = 1L;
        ShibbolethView shibboleth = this.shibbolethManager.getShibbolethConfigById(configId);
        assertThat("Config id mismatch", shibboleth.getId().equals(configId));
    }

    @Test(expected = ShibbolethConfigNotFoundException.class)
    public void shouldNotGetConfigWithWrongId(){
        Long configId = 2L;
        ShibbolethView shibboleth = this.shibbolethManager.getShibbolethConfigById(configId);
    }

    @Test
    public void shouldGetOneConfig(){
        ShibbolethView shibboleth = this.shibbolethManager.getOneShibbolethConfig();
        assertThat("Shibboleth is null", shibboleth != null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotGetOneConfigWhenRepoIsEmpty(){
        when(repo.count()).thenReturn(0L);
        when(repo.findAll()).thenReturn(Collections.emptyList());
        this.shibbolethManager.getOneShibbolethConfig();
    }

    @Test
    public void shouldAddShibbolethConfig(){
        when(repo.count()).thenReturn(0L).thenReturn(1L);
        ShibbolethView shibboleth = new ShibbolethView(5L, "login-url", "logout-url", path,10);
        Shibboleth shibbolethEntity = modelMapper.map(shibboleth, Shibboleth.class);
        when(repo.findAll()).thenReturn(Arrays.asList(shibbolethEntity));
        Long id = this.shibbolethManager.addShibbolethConfig(shibboleth);
        assertThat("Id mismatch", id.equals(shibboleth.getId()));
        verify(repo, times(1)).save(any());
    }

    @Test(expected = OnlyOneShibbolethConfigSupportedException.class)
    public void shouldNotAddShibbolethConfigWhenConfigAlreadyExists(){
        ShibbolethView shibboleth = new ShibbolethView(5L, "login-url", "logout-url", path,10);
        this.shibbolethManager.addShibbolethConfig(shibboleth);
    }

    @Test
    public void shouldUpdateShibbolethConfig(){
        ShibbolethView shibboleth = new ShibbolethView(1L, "login-url", "logout-url", path,10);
        this.shibbolethManager.updateShibbolethConfig(shibboleth.getId(), shibboleth);
        verify(repo, times(1)).save(any());
    }

    @Test(expected = ShibbolethConfigNotFoundException.class)
    public void shouldNotUpdateShibbolethConfigWithWrongId(){
        ShibbolethView shibboleth = new ShibbolethView(2L, "login-url", "logout-url", path,10);
        this.shibbolethManager.updateShibbolethConfig(shibboleth.getId(), shibboleth);
    }

    @Test
    public void shouldRemoveShibbolethConfig(){
        Shibboleth shibboleth = new Shibboleth(1L, "login-url", "logout-url", path,10);
        when(repo.findById(shibboleth.getId())).thenReturn(Optional.of(shibboleth));
        this.shibbolethManager.removeShibbolethConfig(1L);
        verify(repo, times(1)).delete(shibboleth);
    }

    @Test(expected = ShibbolethConfigNotFoundException.class)
    public void shouldNotRemoveShibbolethConfigWithWrongId(){
        this.shibbolethManager.removeShibbolethConfig(2L);
    }
}
