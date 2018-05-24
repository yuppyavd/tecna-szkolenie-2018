package pl.tecna.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("valo")
public class CompanyUI extends UI {
  private static final long serialVersionUID = 3141839683577002084L;
  
  private static final Logger log = LoggerFactory.getLogger(CompanyUI.class);

  @Autowired
  private CompanyRepository repository;

  private CompanyEntity company;

  private Grid<CompanyEntity> grid = new Grid<>(CompanyEntity.class);
  
  private TextField nameField = new TextField("Name");
  private TextField websiteField = new TextField("Website");
  
  private Button createButton = new Button(VaadinIcons.PLUS, e -> createCompany());
  private Button updateButton = new Button(VaadinIcons.PENCIL, e -> updateCompany());
  private Button deleteButton = new Button(VaadinIcons.TRASH, e -> deleteCompany());
  
  private Binder<CompanyEntity> binder = new Binder<>();

  @Override
  protected void init(VaadinRequest request) {
    updateGrid();
    grid.addSelectionListener(e -> updateForm());
    
    HorizontalLayout buttonsLayout = new HorizontalLayout(createButton, updateButton, deleteButton);
    VerticalLayout formLayout = new VerticalLayout(nameField, websiteField, buttonsLayout);
    HorizontalLayout layout = new HorizontalLayout(grid, formLayout);
    layout.setMargin(true);
    layout.setSpacing(true);
    setContent(layout);

    binder.bind(nameField, CompanyEntity::getName, CompanyEntity::setName);
    binder.bind(websiteField, CompanyEntity::getWebsite, CompanyEntity::setWebsite);
  }

  private void updateGrid() {
    grid.setItems(repository.findAll());
  }

  private void updateForm() {
    if (!grid.getSelectedItems().isEmpty()) {
      company = grid.getSelectedItems().iterator().next();
      binder.readBean(company);
    }
  }
  
  private void createCompany() {
    log.info("Create company");
    
    try {
      CompanyEntity entity = new CompanyEntity();
      binder.writeBean(entity);
      repository.save(entity);
      updateGrid();
    } catch (ValidationException e) {
      log.warn("Cannot create company", e);
    }
  }
  
  private void updateCompany() {
    log.info("Update company");
    
    if (company == null) {
      return;
    }
    
    try {
      binder.writeBean(company);
      repository.save(company);
      updateGrid();
    } catch (ValidationException e) {
      log.warn("Cannot update company", e);
    }
  }
  
  private void deleteCompany() {
    log.info("Delete company");
    
    if (company == null) {
      return;
    }
    
    repository.delete(company);
    updateGrid();
  }
}
