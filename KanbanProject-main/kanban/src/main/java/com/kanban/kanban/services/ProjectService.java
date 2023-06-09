package com.kanban.kanban.services;

import com.kanban.kanban.domain.Project;
import com.kanban.kanban.domain.Task;
import com.kanban.kanban.exception.DuplicateProjectException;
import com.kanban.kanban.exception.ProjectNotFoundException;
import com.kanban.kanban.repository.IProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectService implements IProjectService {
    @Autowired
    IProjectRepository projectRepository;

    @Override
    public Project createProject(Project project) throws DuplicateProjectException {
        if (projectRepository.findById(project.getName()).isEmpty())
            return projectRepository.insert(project);
        throw new DuplicateProjectException();
    }

    @Override
    public Project getProject(String name) throws ProjectNotFoundException {
        if (projectRepository.findById(name).isEmpty())
            throw new ProjectNotFoundException();
        return projectRepository.findById(name).get();
    }

    @Override
    public boolean deleteProject(String name) throws ProjectNotFoundException {
        if (projectRepository.findById(name).isEmpty())
            throw new ProjectNotFoundException();
        projectRepository.deleteById(name);
        return true;
    }

    @Override
    public boolean saveChanges(String name, Map<String, List<Task>> columns) throws ProjectNotFoundException {
        if (projectRepository.findById(name).isEmpty())
            throw new ProjectNotFoundException();
        Project project = projectRepository.findById(name).get();
        project.setColumns(columns);
        projectRepository.save(project);
        return true;
    }

    @Override
    public Project addNewTask(String name, Task task) {
        System.out.println("this is the delete task method ");

        Project project = projectRepository.findById(name).get();
        boolean flag = project.getColumns().get("To Be Done")
                .stream().anyMatch(t -> t.getName().equals(task.getName()));
        if (flag) {
            throw new IllegalArgumentException("Task with the same name already exists");
        }
        project.getColumns().get("To Be Done").add(task);
        return projectRepository.save(project);
    }

    @Override
    public boolean deleteMemeberFromProject(String projectName, String userName) throws ProjectNotFoundException {
        Optional<Project> optionalProject = projectRepository.findById(projectName);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            List<String> listMembers = project.getMembers();
            if (listMembers.contains(userName)) {
                listMembers.removeIf(member -> member.equals(userName));
                if (listMembers.isEmpty()) {
                    deleteProject(projectName);
                } else {
                    project.setMembers(listMembers);
                    projectRepository.save(project);
                }
                return true;
            }
        }
        throw new ProjectNotFoundException();
    }

}
