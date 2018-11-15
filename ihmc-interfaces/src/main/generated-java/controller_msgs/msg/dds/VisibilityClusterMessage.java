package controller_msgs.msg.dds;

import us.ihmc.communication.packets.Packet;
import us.ihmc.euclid.interfaces.Settable;
import us.ihmc.euclid.interfaces.EpsilonComparable;
import java.util.function.Supplier;
import us.ihmc.pubsub.TopicDataType;

/**
       * This message is part of the IHMC footstep planning module.
       */
public class VisibilityClusterMessage extends Packet<VisibilityClusterMessage> implements Settable<VisibilityClusterMessage>, EpsilonComparable<VisibilityClusterMessage>
{
   public static final byte EXTRUSION_INSIDE = (byte) 0;
   public static final byte EXTRUSION_OUTSIDE = (byte) 1;
   public static final byte TYPE_LINE = (byte) 0;
   public static final byte TYPE_MULTI_LINE = (byte) 1;
   public static final byte TYPE_POLYGON = (byte) 2;
   public byte extrusion_side_ = (byte) 255;
   public byte type_ = (byte) 255;
   public us.ihmc.euclid.geometry.Pose3D pose_in_world_;
   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  raw_points_in_local_;
   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  navigable_extrusions_in_local_;
   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  non_navigable_extrusions_in_local_;
   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  rotation_extrusions_in_local_;

   public VisibilityClusterMessage()
   {
      pose_in_world_ = new us.ihmc.euclid.geometry.Pose3D();
      raw_points_in_local_ = new us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D> (25, new geometry_msgs.msg.dds.PointPubSubType());
      navigable_extrusions_in_local_ = new us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D> (25, new geometry_msgs.msg.dds.PointPubSubType());
      non_navigable_extrusions_in_local_ = new us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D> (25, new geometry_msgs.msg.dds.PointPubSubType());
      rotation_extrusions_in_local_ = new us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D> (25, new geometry_msgs.msg.dds.PointPubSubType());

   }

   public VisibilityClusterMessage(VisibilityClusterMessage other)
   {
      this();
      set(other);
   }

   public void set(VisibilityClusterMessage other)
   {
      extrusion_side_ = other.extrusion_side_;

      type_ = other.type_;

      geometry_msgs.msg.dds.PosePubSubType.staticCopy(other.pose_in_world_, pose_in_world_);
      raw_points_in_local_.set(other.raw_points_in_local_);
      navigable_extrusions_in_local_.set(other.navigable_extrusions_in_local_);
      non_navigable_extrusions_in_local_.set(other.non_navigable_extrusions_in_local_);
      rotation_extrusions_in_local_.set(other.rotation_extrusions_in_local_);
   }

   public void setExtrusionSide(byte extrusion_side)
   {
      extrusion_side_ = extrusion_side;
   }
   public byte getExtrusionSide()
   {
      return extrusion_side_;
   }

   public void setType(byte type)
   {
      type_ = type;
   }
   public byte getType()
   {
      return type_;
   }


   public us.ihmc.euclid.geometry.Pose3D getPoseInWorld()
   {
      return pose_in_world_;
   }


   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  getRawPointsInLocal()
   {
      return raw_points_in_local_;
   }


   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  getNavigableExtrusionsInLocal()
   {
      return navigable_extrusions_in_local_;
   }


   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  getNonNavigableExtrusionsInLocal()
   {
      return non_navigable_extrusions_in_local_;
   }


   public us.ihmc.idl.IDLSequence.Object<us.ihmc.euclid.tuple3D.Point3D>  getRotationExtrusionsInLocal()
   {
      return rotation_extrusions_in_local_;
   }


   public static Supplier<VisibilityClusterMessagePubSubType> getPubSubType()
   {
      return VisibilityClusterMessagePubSubType::new;
   }

   @Override
   public Supplier<TopicDataType> getPubSubTypePacket()
   {
      return VisibilityClusterMessagePubSubType::new;
   }

   @Override
   public boolean epsilonEquals(VisibilityClusterMessage other, double epsilon)
   {
      if(other == null) return false;
      if(other == this) return true;

      if (!us.ihmc.idl.IDLTools.epsilonEqualsPrimitive(this.extrusion_side_, other.extrusion_side_, epsilon)) return false;

      if (!us.ihmc.idl.IDLTools.epsilonEqualsPrimitive(this.type_, other.type_, epsilon)) return false;

      if (!this.pose_in_world_.epsilonEquals(other.pose_in_world_, epsilon)) return false;
      if (this.raw_points_in_local_.size() != other.raw_points_in_local_.size()) { return false; }
      else
      {
         for (int i = 0; i < this.raw_points_in_local_.size(); i++)
         {  if (!this.raw_points_in_local_.get(i).epsilonEquals(other.raw_points_in_local_.get(i), epsilon)) return false; }
      }

      if (this.navigable_extrusions_in_local_.size() != other.navigable_extrusions_in_local_.size()) { return false; }
      else
      {
         for (int i = 0; i < this.navigable_extrusions_in_local_.size(); i++)
         {  if (!this.navigable_extrusions_in_local_.get(i).epsilonEquals(other.navigable_extrusions_in_local_.get(i), epsilon)) return false; }
      }

      if (this.non_navigable_extrusions_in_local_.size() != other.non_navigable_extrusions_in_local_.size()) { return false; }
      else
      {
         for (int i = 0; i < this.non_navigable_extrusions_in_local_.size(); i++)
         {  if (!this.non_navigable_extrusions_in_local_.get(i).epsilonEquals(other.non_navigable_extrusions_in_local_.get(i), epsilon)) return false; }
      }

      if (this.rotation_extrusions_in_local_.size() != other.rotation_extrusions_in_local_.size()) { return false; }
      else
      {
         for (int i = 0; i < this.rotation_extrusions_in_local_.size(); i++)
         {  if (!this.rotation_extrusions_in_local_.get(i).epsilonEquals(other.rotation_extrusions_in_local_.get(i), epsilon)) return false; }
      }


      return true;
   }

   @Override
   public boolean equals(Object other)
   {
      if(other == null) return false;
      if(other == this) return true;
      if(!(other instanceof VisibilityClusterMessage)) return false;

      VisibilityClusterMessage otherMyClass = (VisibilityClusterMessage) other;

      if(this.extrusion_side_ != otherMyClass.extrusion_side_) return false;

      if(this.type_ != otherMyClass.type_) return false;

      if (!this.pose_in_world_.equals(otherMyClass.pose_in_world_)) return false;
      if (!this.raw_points_in_local_.equals(otherMyClass.raw_points_in_local_)) return false;
      if (!this.navigable_extrusions_in_local_.equals(otherMyClass.navigable_extrusions_in_local_)) return false;
      if (!this.non_navigable_extrusions_in_local_.equals(otherMyClass.non_navigable_extrusions_in_local_)) return false;
      if (!this.rotation_extrusions_in_local_.equals(otherMyClass.rotation_extrusions_in_local_)) return false;

      return true;
   }

   @Override
   public java.lang.String toString()
   {
      StringBuilder builder = new StringBuilder();

      builder.append("VisibilityClusterMessage {");
      builder.append("extrusion_side=");
      builder.append(this.extrusion_side_);      builder.append(", ");
      builder.append("type=");
      builder.append(this.type_);      builder.append(", ");
      builder.append("pose_in_world=");
      builder.append(this.pose_in_world_);      builder.append(", ");
      builder.append("raw_points_in_local=");
      builder.append(this.raw_points_in_local_);      builder.append(", ");
      builder.append("navigable_extrusions_in_local=");
      builder.append(this.navigable_extrusions_in_local_);      builder.append(", ");
      builder.append("non_navigable_extrusions_in_local=");
      builder.append(this.non_navigable_extrusions_in_local_);      builder.append(", ");
      builder.append("rotation_extrusions_in_local=");
      builder.append(this.rotation_extrusions_in_local_);
      builder.append("}");
      return builder.toString();
   }
}
