package ewm.mapper.event;

import ewm.dto.event.EventFullDto;
import ewm.dto.event.EventShortDto;
import ewm.dto.event.LocationDto;
import ewm.dto.event.NewEventDto;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.event.Location;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto newEventDto, User initiator, Category category) {
        Event event = new Event();
        event.setTitle(newEventDto.getTitle());
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setPaid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.setEventDate(newEventDto.getEventDate());
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(null);
        event.setParticipantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.setRequestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.setState(EventState.PENDING);

        Location location = new Location();
        location.setLat(newEventDto.getLocation().getLat());
        location.setLon(newEventDto.getLocation().getLon());
        event.setLocation(location);

        return event;
    }

    public static EventFullDto toFullDto(Event event, int confirmedRequests, long views) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setDescription(event.getDescription());

        CategoryDto categoryDto = new CategoryDto(); // проверить
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        eventFullDto.setCategory(categoryDto);

        UserShortDto userShortDto = new UserShortDto(); // проверить
        userShortDto.setId(event.getInitiator().getId());
        userShortDto.setName(event.getInitiator().getName());
        eventFullDto.setInitiator(userShortDto);

        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState());

        LocationDto locationDto = new LocationDto();
        locationDto.setLat(event.getLocation().getLat());
        locationDto.setLon(event.getLocation().getLon());
        eventFullDto.setLocation(locationDto);

        eventFullDto.setConfirmedRequests(confirmedRequests);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    public static EventShortDto toShortDto(Event event, int confirmedRequests, long views) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setEventDate(event.getEventDate());

        CategoryDto categoryDto = new CategoryDto(); // проверить
        categoryDto.setId(event.getCategory().getId());
        categoryDto.setName(event.getCategory().getName());
        eventShortDto.setCategory(categoryDto);

        UserShortDto userShortDto = new UserShortDto(); // проверить
        userShortDto.setId(event.getInitiator().getId());
        userShortDto.setName(event.getInitiator().getName());
        eventShortDto.setInitiator(userShortDto);

        eventShortDto.setConfirmedRequests(confirmedRequests);
        eventShortDto.setViews(views);
    }

}
