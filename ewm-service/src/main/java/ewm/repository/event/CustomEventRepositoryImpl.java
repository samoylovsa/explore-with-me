package ewm.repository.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ewm.dto.event.AdminEventParameters;
import ewm.model.event.Event;
import ewm.model.event.QEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CustomEventRepositoryImpl implements CustomEventRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findAdminEvents(AdminEventParameters adminEventParameters, Pageable pageable) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (adminEventParameters.getUsers() != null && !adminEventParameters.getUsers().isEmpty())
            booleanBuilder.and(QEvent.event.initiator.id.in(adminEventParameters.getUsers()));
        if (adminEventParameters.getStates() != null && !adminEventParameters.getStates().isEmpty())
            booleanBuilder.and(QEvent.event.state.in(adminEventParameters.getStates()));
        if (adminEventParameters.getCategories() != null && !adminEventParameters.getCategories().isEmpty())
            booleanBuilder.and(QEvent.event.category.id.in(adminEventParameters.getCategories()));
        if (adminEventParameters.getRangeStart() != null)
            booleanBuilder.and(QEvent.event.eventDate.goe(adminEventParameters.getRangeStart()));
        if (adminEventParameters.getRangeEnd() != null)
            booleanBuilder.and(QEvent.event.eventDate.loe(adminEventParameters.getRangeEnd()));
        return queryFactory.selectFrom(QEvent.event)
                .where(booleanBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
