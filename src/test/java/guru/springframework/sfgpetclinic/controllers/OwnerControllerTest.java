package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.fauxspring.BindingResult;
import guru.springframework.sfgpetclinic.fauxspring.Model;
import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final String OWNERS_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String REDIRECT_OWNERS_5 = "redirect:/owners/5";

    @Mock
    OwnerService ownerService;

    @InjectMocks
    OwnerController controller;

    @Mock
    BindingResult bindingResult;

    @Mock
    Model model;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp(){
        given(ownerService.findAllByLastNameLike(stringArgumentCaptor.capture()))
        .willAnswer( invocation -> {
            List<Owner> owners = new ArrayList<>();
            String name = invocation.getArgument(0);
            if( name.equals("%Buck%")){
                owners.add(new Owner(1L, "Joe", "Buck"));
                return owners;
            } else if ( name.equals("%DontFindMe%") ){
                return owners;
            }  else if ( name.equals("%FindMe%") ){
                owners.add(new Owner(1L, "Joe", "Buck"));
                owners.add(new Owner(1L, "Joe2", "Buck2"));
                return owners;
            }
            throw  new RuntimeException("Invalid Argument");
        });
    }

    @Test
    void processCreationFormWildcardFound(){
        // given
        Owner owner = new Owner(1L, "Joe", "FindMe");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, model);

        // then
        assertThat("%FindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("owners/ownersList").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processCreationFormWildcardsStringAnnotation(){
        // given
        Owner owner = new Owner(1L, "Joe", "Buck");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%Buck%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("redirect:/owners/1").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processCreationFormWildcardNotFound(){
        // given
        Owner owner = new Owner(1L, "Joe", "DontFindMe");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%DontFindMe%").isEqualToIgnoringCase(stringArgumentCaptor.getValue());
        assertThat("owners/findOwners").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processCreationFormWildcardsString(){
        // given
        Owner owner = new Owner(1L, "Joe", "Buck");
        List<Owner> ownerList = new ArrayList<>();
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        given(ownerService.findAllByLastNameLike(captor.capture())).willReturn(ownerList);

        // when
       controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%Buck%").isEqualToIgnoringCase(captor.getValue());
    }

    @Test
    void processCreationFormHasErrors(){
        // given
        Owner owner = new Owner(1L, "Jim", "Bob");
        given(bindingResult.hasErrors()).willReturn(true);

        // When
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(OWNERS_CREATE_OR_UPDATE_OWNER_FORM);
    }


    @Test
    void processCreationFormNoErrors() {
        // given
        Owner owner = new Owner(5L, "Jim", "Bob");
        given(bindingResult.hasErrors()).willReturn(false);
        given(ownerService.save(any())).willReturn((owner));

        // When
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(REDIRECT_OWNERS_5);
    }
}