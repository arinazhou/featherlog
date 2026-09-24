package com.arinazhou.featherlog.bird;

import com.arinazhou.featherlog.bird.BirdDtos.BirdRequest;
import com.arinazhou.featherlog.care.CareTaskService;
import com.arinazhou.featherlog.common.NotFoundException;
import com.arinazhou.featherlog.user.UserRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BirdService {

    // A healthy adult pet budgerigar typically weighs 30-40 g; English budgies run heavier.
    static final String DEFAULT_SPECIES = "Budgerigar";
    static final double DEFAULT_MIN_GRAMS = 30.0;
    static final double DEFAULT_MAX_GRAMS = 40.0;

    private final BirdRepository birds;
    private final UserRepository users;
    private final CareTaskService careTasks;
    private final Clock clock;

    public BirdService(BirdRepository birds, UserRepository users, CareTaskService careTasks, Clock clock) {
        this.birds = birds;
        this.users = users;
        this.careTasks = careTasks;
        this.clock = clock;
    }

    /** Loads a bird only if it belongs to the caller; otherwise 404 so ids of other users' birds don't leak. */
    @Transactional(readOnly = true)
    public Bird getOwned(Long ownerId, Long birdId) {
        return birds.findByIdAndOwnerId(birdId, ownerId).orElseThrow(() -> new NotFoundException("Bird", birdId));
    }

    @Transactional(readOnly = true)
    public List<Bird> list(Long ownerId) {
        return birds.findByOwnerIdOrderByNameAsc(ownerId);
    }

    @Transactional
    public Bird create(Long ownerId, BirdRequest request) {
        Bird bird = new Bird(users.getReferenceById(ownerId), clock.instant());
        apply(bird, request);
        Bird saved = birds.save(bird);
        careTasks.seedDefaults(saved);
        return saved;
    }

    @Transactional
    public Bird update(Long ownerId, Long birdId, BirdRequest request) {
        Bird bird = getOwned(ownerId, birdId);
        apply(bird, request);
        return bird;
    }

    @Transactional
    public void delete(Long ownerId, Long birdId) {
        birds.delete(getOwned(ownerId, birdId));
    }

    private static void apply(Bird bird, BirdRequest r) {
        bird.setName(r.name().trim());
        bird.setSpecies(r.species() == null || r.species().isBlank() ? DEFAULT_SPECIES : r.species().trim());
        bird.setColorMutation(r.colorMutation());
        bird.setSex(r.sex() == null ? Sex.UNKNOWN : r.sex());
        bird.setHatchDate(r.hatchDate());
        bird.setTargetRange(
                r.targetMinGrams() == null ? DEFAULT_MIN_GRAMS : r.targetMinGrams(),
                r.targetMaxGrams() == null ? DEFAULT_MAX_GRAMS : r.targetMaxGrams());
        bird.setNotes(r.notes());
    }
}
