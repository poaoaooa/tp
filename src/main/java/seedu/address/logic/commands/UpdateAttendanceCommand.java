package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Week;
import seedu.address.model.person.WeekList;
import seedu.address.model.person.WeeklyAttendanceList;

/**
 * Marks the specified week (tutorial) as attended or not attended for a person.
 */
public class UpdateAttendanceCommand extends Command {

    public static final String COMMAND_WORD = "updateattendance";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Updates attendance of a student.\n"
            + "Parameters: INDEX (positive integer) "
            + PREFIX_WEEK + "WEEK_NUMBER "
            + PREFIX_STATUS + "STATUS (y = attended, a = absent, n = not marked)\n"
            + "Example: " + COMMAND_WORD + " 1 week/5 sta/y";

    public static final String MESSAGE_ATTENDED =
            "Week %1$d marked as attended for: %2$s";
    public static final String MESSAGE_ABSENT =
            "Week %1$d marked as absent for: %2$s";
    public static final String MESSAGE_RESET =
            "Week %1$d reset to unmarked for: %2$s";
    public static final String MESSAGE_DUPLICATE =
            "Week %1$d already has this status for %2$s";


    public final Index index;
    public final Index weekNumber;
    public final Week.Status status;

    /**
     * Creates an UpdateAttendanceCommand to update the attendance status
     * of a specific week for a student identified by their index.
     *
     * @param index Index of the student in the displayed person list.
     * @param weekNumber Index of the week to update (1-based index from user input).
     * @param status The attendance status to assign for the specified week.
     */
    public UpdateAttendanceCommand(Index index, Index weekNumber, Week.Status status) {
        requireAllNonNull(index, weekNumber, status);
        this.index = index;
        this.weekNumber = weekNumber;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException("Invalid person index");
        }
        if (weekNumber.getZeroBased() >= WeekList.NUMBER_OF_WEEKS) {
            throw new CommandException("Invalid Week, there are only 13 weeks");
        }
        Person personToEdit = lastShownList.get(index.getZeroBased());

        // Copy current attendance list
        WeeklyAttendanceList weekList = ((WeekList) personToEdit
                .getWeeklyAttendanceList()).copy();

        // update attendance
        try {
            applyStatusUpdate(weekList);
        } catch (IllegalStateException e) {
            throw new CommandException(
                    String.format(MESSAGE_DUPLICATE,
                            weekNumber.getOneBased(), formatPerson(personToEdit)));
        }

        // new updated person
        Person editedPerson = new Person(
                personToEdit.getName(),
                personToEdit.getCourseId(),
                personToEdit.getEmail(),
                personToEdit.getStudentId(),
                personToEdit.getTGroup(),
                personToEdit.getTele(),
                weekList
        );

        model.setPerson(personToEdit, editedPerson);
        return new CommandResult(generateSuccessMessage(personToEdit));
    }

    /**
     * Applies the correct attendance update based on status.
     */
    private void applyStatusUpdate(WeeklyAttendanceList list) throws CommandException {
        int index = weekNumber.getZeroBased();

        switch (status) {
        case Y:
            list.markWeekAsAttended(index);
            break;
        case A:
            list.markWeekAsAbsent(index);
            break;
        case N:
            list.markWeekAsDefault(index);
            break;
        default:
            throw new CommandException("Invalid status");
        }
    }

    /**
     * Generates appropriate success message.
     */
    private String generateSuccessMessage(Person person) throws CommandException {
        String name = formatPerson(person);
        int week = weekNumber.getOneBased();

        switch (status) {
        case Y:
            return String.format(MESSAGE_ATTENDED, week, name);
        case A:
            return String.format(MESSAGE_ABSENT, week, name);
        case N:
            return String.format(MESSAGE_RESET, week, name);
        default:
            throw new CommandException("Invalid status");
        }
    }

    private String formatPerson(Person person) {
        return person.getName() + " (" + person.getStudentId() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UpdateAttendanceCommand)) {
            return false;
        }
        UpdateAttendanceCommand otherCommand = (UpdateAttendanceCommand) other;
        return index.equals(otherCommand.index)
                && weekNumber.equals(otherCommand.weekNumber)
                && status == otherCommand.status;
    }
}
