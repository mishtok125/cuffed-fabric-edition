package com.lazrproductions.cuffed.client.particle;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** A falling drop of blood; switches to the second sprite when it lands. */
public class BloodDripParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    BloodDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(0, 1));
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.sprites = sprites;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0)
            this.remove();
        if (!this.removed) {
            this.yd -= (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (!this.removed) {
                this.xd *= (double) 0.98F;
                this.yd *= (double) 0.98F;
                this.zd *= (double) 0.98F;
            }
        }
        if (this.onGround)
            this.setSprite(this.sprites.get(1, 1));
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet set) {
            this.sprites = set;
        }

        @Override
        @Nullable
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xAux, double yAux, double zAux, RandomSource random) {
            BloodDripParticle particle = new BloodDripParticle(level, x, y, z, sprites);
            particle.setColor(0.74F, 0.074F, 0.074F);
            return particle;
        }
    }
}
